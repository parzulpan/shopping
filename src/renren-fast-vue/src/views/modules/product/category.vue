<!--  -->

<template>
  <div>
    <el-tree
      :data="menus"
      :props="defaultProps"
      :expand-on-click-node="false"
      show-checkbox
      node-key="catId"
      :default-expanded-keys="expandedKey"
      draggable
      :allow-drop="allowDrop"
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
            添加
          </el-button>
          <el-button
            v-if="node.childNodes.length == 0"
            type="text"
            size="mini"
            @click="() => remove(node, data)"
          >
            删除
          </el-button>
          <el-button
            v-if="true"
            type="text"
            size="mini"
            @click="() => edit(node, data)"
          >
            修改
          </el-button>
        </span>
      </span>
    </el-tree>
    <el-dialog
      :title="title"
      :visible.sync="dialogVisible"
      width="30%"
      :close-on-click-modal="false"
    >
      <el-form :model="category" label-width="80px">
        <el-form-item label="分类名称">
          <el-input v-model="category.name"></el-input>
        </el-form-item>
        <el-form-item label="分类图标">
          <el-input v-model="category.icon"></el-input>
        </el-form-item>
        <el-form-item label="计量单位">
          <el-input v-model="category.productUnit"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitData">确 定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
// 这里可以导入其他文件（比如：组件，工具js，第三方插件js，json文件，图片文件等等）
// 例如：import 《组件名称》 from '《组件路径》';
export default {
  // import引入的组件需要注入到对象中才能使用
  components: {},
  // 这里传递属性
  props: {},
  // 这里存放数据
  data () {
    return {
      maxLevel: 0,
      title: '',
      dialogType: '', // edit, add
      category: { name: '', parentCid: 0, catLevel: 0, showStatus: 1, sort: 0, icon: '', productUnit: '', catId: null },
      dialogVisible: false,
      menus: [],
      expandedKey: [],
      defaultProps: {
        children: 'children',
        label: 'name'
      }
    }
  },
  // 方法集合
  methods: {
    // 获得菜单列表
    getMenus () {
      this.$http({
        url: this.$http.adornUrl('/product/category/list/tree'),
        method: 'get'
      }).then(({ data }) => {
        // console.log('成功获取到菜单数据... \n', data.data)
        if (data && data.data) {
          this.menus = data.data
        } else {
          this.menus = []
        }
      })
    },

    allowDrop (draggingNode, dropNode, type) {
      // 被拖动的当前节点以及所在父节点的总层层不能大于 3
      // console.log(draggingNode, dropNode, type)

      this.countNodeLevel(draggingNode.data)
      let currentLevel = this.maxLevel - draggingNode.data.catLevel + 1
      console.log(currentLevel)

      if (type === 'inner') {
        return (currentLevel + dropNode.level) <= 3
      } else {
        return (currentLevel + dropNode.parent.level) <= 3
      }
    },

    countNodeLevel (node) {
      if (node.children != null && node.children.length > 0) {
        for (let i = 0; i < node.children.length; ++i) {
          if (node.children[i].catLevel > this.maxLevel) {
            this.maxLevel = node.children[i].catLevel
          }
          this.countNodeLevel(node.children[i])
        }
      }
    },

    submitData () {
      if (this.dialogType === 'add') {
        this.addCateaory()
      }

      if (this.dialogType === 'edit') {
        this.editCateaory()
      }
    },

    edit (node, data) {
      console.log('old data', data)

      this.dialogType = 'edit'
      this.title = '修改分类'
      this.dialogVisible = true
      // 发送请求获取当前节点最新的数据

      this.$http({
        url: this.$http.adornUrl(`/product/category/info/${data.catId}`),
        method: 'get'
      }).then(({ data }) => {
        console.log('new data', data)
        this.category.name = data.category.name
        this.category.catId = data.category.catId
        this.category.icon = data.category.icon
        this.category.productUnit = data.category.productUnit
        this.category.parentCid = data.category.parentCid
        this.category.catLevel = data.category.catLevel
        this.category.sort = data.category.sort
        this.category.showStatus = data.category.showStatus
      })
    },

    append (data) {
      console.log('data', data)

      this.dialogType = 'add'
      this.title = '添加分类'
      this.dialogVisible = true
      this.category.parentCid = data.catId
      this.category.catLevel = data.catLevel * 1 + 1

      this.category.name = ''
      this.category.catId = null
      this.category.icon = ''
      this.category.productUnit = ''
      this.category.sort = 0
      this.category.showStatus = 1
    },

    // 修改三级分类
    editCateaory () {
      console.log('修改三级分类 ', this.category)

      let { catId, name, icon, productUnit } = this.category
      // let data = { catId: catId, name: name, icon: icon, productUnit: productUnit }
      this.$http({
        url: this.$http.adornUrl('/product/category/update'),
        method: 'post',
        data: this.$http.adornData({ catId, name, icon, productUnit }, false)
      }).then(({ data }) => {
        this.$message({
          type: 'success',
          message: '菜单修改成功!'
        })
        this.dialogVisible = false
        this.getMenus()
        this.expandedKey = [this.category.parentCid]
      })
    },

    // 添加三级分类
    addCateaory () {
      console.log('添加三级分类 ', this.category)

      this.$http({
        url: this.$http.adornUrl('/product/category/save'),
        method: 'post',
        data: this.$http.adornData(this.category, false)
      }).then(({ data }) => {
        this.$message({
          type: 'success',
          message: '菜单添加成功！'
        })
        this.dialogVisible = false
        this.getMenus()
        this.expandedKey = [this.category.parentCid]
      })
    },

    remove (node, data) {
      console.log('old data', data)

      let ids = [data.catId]

      this.$confirm(`此操作将永久删除 [ ${data.name} ] 菜单, 是否继续?`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$http({
          url: this.$http.adornUrl('/product/category/delete'),
          method: 'post',
          data: this.$http.adornData(ids, false)
        }).then(({ data }) => {
          this.$message({
            type: 'success',
            message: '菜单删除成功！'
          })
          // 刷新出新的菜单
          this.getMenus()
          // 设置需要默认展开的菜单
          this.expandedKey = [node.parent.data.catId]
        })
      }).catch(() => {
        this.$message({
          type: 'info',
          message: '取消删除！'
        })
      })
    }
  },
  // 监听属性 类似于data概念
  computed: {},
  // 监控data中的数据变化
  watch: {},
  // 生命周期 - 创建完成（可以访问当前this实例）
  created () {
    this.getMenus()
  },
  // 生命周期 - 创建之前
  beforeCreate () { },
  // 生命周期 - 挂载之前
  beforeMount () { },
  // 生命周期 - 更新之前
  beforeUpdate () { },
  // 生命周期 - 更新之后
  updated () { },
  // 生命周期 - 销毁之前
  beforeDestroy () { },
  // 生命周期 - 销毁完成
  destroyed () { },
  // 如果页面有keep-alive缓存功能，这个函数会触发
  activated () { }
}
</script>

<style>
</style>