 /**
 * v-hasPermi 操作权限处理
 * Copyright (c) 2019 ruoyi
 */

import store from '@/store'

export default {
  inserted(el, binding, vnode) {
    const { value } = binding
    //顶级权限
    const all_permission = "*:*:*";
    //十分优雅的写法,仓库中存在getters,也存在getters的权限值.
    const permissions = store.getters && store.getters.permissions
    // value 存在,且value是数据,且value的长度大于0,
    if (value && value instanceof Array && value.length > 0) {
      const permissionFlag = value

      const hasPermissions = permissions.some(permission => {
        //如果是all_permission就直接返回,否则就去看包含那个权限
        return all_permission === permission || permissionFlag.includes(permission)
      })

      if (!hasPermissions) {
        el.parentNode && el.parentNode.removeChild(el)
      }
    } else {
      throw new Error(`请设置操作权限标签值`)
    }
  }
}
