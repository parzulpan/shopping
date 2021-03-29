<!--商品系统-分类维护-->
<template>
  <div>
    <el-switch
      v-model="draggable"
      active-text="开启拖拽"
      inactive-text="关闭拖拽"
    >
    </el-switch>
    <el-button v-if="draggable" @click="batchSave">批量保存结果</el-button>
    <el-button type="danger" @click="batchDelete">批量删除菜单</el-button>
    <el-tree
      :data="menus"
      :props="defaultProps"
      @node-click="handleNodeClick"
      :expand-on-click-node="false"
      show-checkbox
      node-key="catId"
      :default-expanded-keys="expandedKey"
      :draggable="draggable"
      :allow-drop="allowDrop"
      @node-drop="handleDrop"
      ref="menuTree"
    >
      <span class="custom-tree-node" slot-scope="{ node, data }">
        <span>{{ node.label }}</span>
        <span>
          <el-button
            v-if="node.level <= 2"
            type="text"
            size="mini"
            @click="() => append(data)"
          >
            添加子菜单
          </el-button>
          <el-button
            v-if="node.childNodes.length == 0"
            type="text"
            size="mini"
            @click="() => remove(node, data)"
          >
            删除该菜单
          </el-button>
          <el-button type="text" size="mini" @click="() => edit(data)">
            修改该菜单
          </el-button>
        </span>
      </span>
    </el-tree>

    <el-dialog
      :title="dialogTitle"
      :visible.sync="dialogFormVisible"
      width="30%"
      :close-on-click-modal="false"
    >
      <el-form :model="category">
        <el-form-item label="请填写分类名称：">
          <el-input v-model="category.name" autocomplete="off"></el-input>
        </el-form-item>
      </el-form>
      <el-form :model="category">
        <el-form-item label="请填写计量单位：">
          <el-input
            v-model="category.productUnit"
            autocomplete="off"
          ></el-input>
        </el-form-item>
      </el-form>
      <el-form :model="category">
        <el-form-item label="请填写图标：">
          <el-input v-model="category.icon" autocomplete="off"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogFormVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitData">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  // import引入的组件需要注入到对象中才能使用
  components: {},
  data() {
    return {
      menus: [],
      expandedKey: [],
      dialogFormVisible: false,
      dialogType: "", // edit、add
      dialogTitle: "",
      category: {
        name: "",
        parentCid: 0,
        catLevel: 0,
        showStatus: 1,
        sort: 0,
        productUnit: "",
        icon: "",
        catId: null,
      },
      maxLevel: 0,
      updateNodes: [],
      draggable: false,
      pCid: [],
      defaultProps: {
        children: "children",
        label: "name",
      },
    };
  },
  methods: {
    // 获取菜单列表
    getMenus() {
      this.$http({
        url: this.$http.adornUrl("/product/category/list/tree"),
        method: "get",
        params: this.$http.adornParams({}),
      }).then(({ data }) => {
        console.log("成功获取到菜单数据... ", data);
        this.menus = data.data;
      });
    },

    // 判断提示框的功能
    submitData() {
      if (this.dialogType == "add") {
        this.addCategory();
      }
      if (this.dialogType == "edit") {
        this.editCategory();
      }
    },

    // 添加子菜单
    append(data) {
      this.dialogType = "add";
      this.dialogTitle = "添加子菜单";
      this.dialogFormVisible = true;
      this.category.parentCid = data.catId;
      this.category.catLevel = data.catLevel * 1 + 1;
      this.category.catId = null;
      this.category.name = "";
      this.category.icon = "";
      this.category.productUnit = "";
      this.category.sort = 0;
      this.category.showStatus = 1;
    },
    // 添加三级分类
    addCategory() {
      console.log("提交的数据为", this.category);
      this.$http({
        url: this.$http.adornUrl("/product/category/save"),
        method: "post",
        data: this.$http.adornData(this.category, false),
      }).then(({ data }) => {
        this.dialogFormVisible = false;
        this.getMenus();
        this.expandedKey = [this.category.parentCid];
      });
      this.$message({
        type: "success",
        message: "添加成功!",
      });
    },

    // 删除该菜单
    remove(node, data) {
      var ids = [data.catId];

      this.$confirm(`是否删除【${data.name}】菜单？`, "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      })
        .then(() => {
          this.$http({
            url: this.$http.adornUrl("/product/category/delete"),
            method: "post",
            data: this.$http.adornData(ids, false),
          }).then(({ data }) => {
            // console.log("删除该菜单成功！");
            this.getMenus();
            this.expandedKey = [node.parent.data.catId];
          });
          this.$message({
            type: "success",
            message: "删除成功!",
          });
        })
        .catch(() => {
          // this.$message({
          //   type: "info",
          //   message: "已取消删除",
          // });
        });
    },

    // 修改该菜单
    edit(data) {
      // console.log("要修改的数据为：", data);
      this.dialogType = "edit";
      this.dialogTitle = "修改该菜单";
      this.dialogFormVisible = true;

      // 发送请求获取当前节点最新的数据
      this.$http({
        url: this.$http.adornUrl(`/product/category/info/${data.catId}`),
        method: "get",
        params: this.$http.adornParams({}),
      }).then(({ data }) => {
        console.log("请求成功，回显的数据为：", data);
        this.category.name = data.data.name;
        this.category.catId = data.data.catId;
        this.category.icon = data.data.icon;
        this.category.productUnit = data.data.productUnit;
        this.category.parentCid = data.data.parentCid;
      });
    },
    // 修改三级分类
    editCategory() {
      var { name, catId, icon, productUnit } = this.category;
      this.$http({
        url: this.$http.adornUrl("/product/category/update"),
        method: "post",
        data: this.$http.adornData({ name, catId, icon, productUnit }, false),
      }).then(({ data }) => {
        this.dialogFormVisible = false;
        this.getMenus();
        this.expandedKey = [this.category.parentCid];
      });
      this.$message({
        type: "success",
        message: "修改成功!",
      });
    },

    // 是否允许拖拽到目标结点
    allowDrop(draggingNode, dropNode, type) {
      // 被拖动的当前节点以及所在父节点的总层数不能大于3
      // console.log("allowDrop:", draggingNode, dropNode, type);
      this.countNodeLevel(draggingNode);
      let deep = Math.abs(this.maxLevel - draggingNode.level) + 1;
      if (type == "inner") {
        return deep + dropNode.level <= 3;
      } else {
        return deep + dropNode.parent.level <= 3;
      }
    },
    // 求该节点的最大深度
    countNodeLevel(node) {
      if (node.childNodes != null && node.childNodes.length > 0) {
        for (let i = 0; i < node.childNodes.length; i++) {
          if (node.childNodes[i].level > this.maxLevel) {
            this.maxLevel = node.childNodes[i].level;
          }
          this.countNodeLevel(node.childNodes[i]);
        }
      }
    },

    // 拖拽成功处理函数
    handleDrop(draggingNode, dropNode, dropType, ev) {
      // console.log("tree drop: ", draggingNode, dropNode, dropType, ev);
      // 1. 当前节点最新的父节点 id
      let pCid = 0;
      let siblings = null;
      if (dropType == "before" || dropType == "after") {
        pCid =
          dropNode.parent.data.catId == undefined
            ? 0
            : dropNode.parent.data.catId;
        siblings = dropNode.parent.childNodes;
      } else {
        pCid = dropNode.data.catId;
        siblings = dropNode.childNodes;
      }
      this.pCid.push(pCid);

      // 2. 当前拖拽节点的最新顺序
      for (let i = 0; i < siblings.length; i++) {
        // 如果遍历的是当前正在拖拽的节点
        if (siblings[i].data.catId == draggingNode.data.catId) {
          let catLevel = draggingNode.level;
          if (siblings[i].level != draggingNode.level) {
            // 当前节点的层级发生变化
            catLevel = siblings[i].level;
            // 修改子节点的层级
            this.updateChildNodeLevel(siblings[i]);
          }
          this.updateNodes.push({
            catId: siblings[i].data.catId,
            sort: i,
            parentCid: pCid,
            catLevel: catLevel,
          });
        } else {
          this.updateNodes.push({ catId: siblings[i].data.catId, sort: i });
        }
      }

      // 3. 当前拖拽节点的最新层级
      // 批量保存拖拽结果时调用
    },
    // 改变子节点的层级
    updateChildNodeLevel(node) {
      for (let i = 0; i < node.childNodes.length; i++) {
        var cNode = node.childNodes[i].data;
        this.updateNodes.push({
          catId: cNode.catId,
          catLevel: node.childNodes[i].level,
        });
        this.updateChildNodeLevel(node.childNodes[i]);
      }
    },

    // 批量保存拖拽结果
    batchSave() {
      this.$http({
        url: this.$http.adornUrl("/product/category/update/sort"),
        method: "post",
        data: this.$http.adornData(this.updateNodes, false),
      }).then(({ data }) => {
        this.getMenus();
        this.expandedKey = this.pCid;
        this.updateNodes = [];
        this.maxLevel = 0;
        // this.pCid = [];
        this.$message({
          type: "success",
          message: "菜单顺序修改成功!",
        });
      });
    },

    // 批量删除菜单
    batchDelete() {
      let catIds = [];
      let checkedNodes = this.$refs.menuTree.getCheckedNodes();
      for (let i = 0; i < checkedNodes.length; i++) {
        catIds.push(checkedNodes[i].catId);
      }
      this.$confirm(`是否批量删除【${catIds}】菜单？`, "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      })
        .then(() => {
          this.$http({
            url: this.$http.adornUrl("/product/category/delete"),
            method: "post",
            data: this.$http.adornData(catIds, false),
          }).then(({ data }) => {
            this.$message({
              message: "批量删除菜单成功",
              type: "success",
            });
            this.getMenus();
          });
        })
        .catch(() => {});
    },
  },

  // 监听属性 类似于data概念
  computed: {},

  // 监控data中的数据变化
  watch: {},

  // 生命周期 - 创建完成（可以访问当前this实例）
  created() {
    this.getMenus();
  },

  // 生命周期 - 挂载完成（可以访问DOM元素
  mounted() {},

  // 生命周期 - 创建之前

  beforeCreate() {},
  // 生命周期 - 挂载之前
  beforeMount() {},

  // 生命周期 - 更新之前
  beforeUpdate() {},

  // 生命周期 - 更新之后
  updated() {},

  // 生命周期 - 销毁之前
  beforeDestroy() {},

  // 生命周期 - 销毁完成
  destroyed() {},

  // 如果页面有keep-alive缓存功能，这个函数会触发
  activated() {},
};
</script>
<style scoped>
</style>