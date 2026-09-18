import { beforeEach, describe, expect, it, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { createI18n } from 'vue-i18n'
import BaseFormDialog from '@/components/common/BaseFormDialog.vue'
import DictTag from '@/components/common/DictTag.vue'
import FxActionGroup from '@/components/common/FxActionGroup.vue'

const i18n = createI18n({
  legacy: false,
  locale: 'zh-CN',
  messages: { 'zh-CN': { common: { cancel: '取消', confirm: '确定', more: '更多' } } },
})

const global = {
  plugins: [i18n],
  stubs: {
    AModal: { template: '<section class="modal"><slot /><slot name="footer" /></section>' },
    ADrawer: { template: '<section class="drawer"><slot /><slot name="footer" /></section>' },
    ASpace: { template: '<div><slot /></div>' },
    AButton: { template: '<button><slot /></button>' },
    ATag: { template: '<span class="tag"><slot /></span>' },
    ADropdown: { template: '<div class="dropdown"><slot /><slot name="overlay" /></div>' },
    AMenu: { template: '<div><slot /></div>' },
    AMenuItem: { template: '<button><slot /></button>' },
    DownOutlined: true,
    FxIcon: true,
  },
}

describe('common components', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('BaseFormDialog emits submit, ok, cancel, and its v-model update', async () => {
    const wrapper = mount(BaseFormDialog, {
      props: { open: true, mode: 'modal', title: '编辑用户' },
      global,
    })

    await wrapper.findAll('button')[1].trigger('click')
    await wrapper.find('button').trigger('click')

    expect(wrapper.emitted('submit')).toHaveLength(1)
    expect(wrapper.emitted('ok')).toHaveLength(1)
    expect(wrapper.emitted('cancel')).toHaveLength(1)
    expect(wrapper.emitted('update:open')?.[0]).toEqual([false])
  })

  it('DictTag renders a matching dictionary label and the fallback for an unknown value', () => {
    const matched = mount(DictTag, {
      props: { items: [{ value: 1, label: '启用' }], value: '1', fallbackText: '未知' },
      global,
    })
    const unmatched = mount(DictTag, {
      props: { items: [{ value: 1, label: '启用' }], value: '2', fallbackText: '未知' },
      global,
    })

    expect(matched.text()).toContain('启用')
    expect(unmatched.text()).toBe('未知')
  })

  it('FxActionGroup collapses excess actions and does not invoke disabled actions', async () => {
    const enabled = vi.fn()
    const disabled = vi.fn()
    const wrapper = mount(FxActionGroup, {
      props: {
        maxInline: 1,
        actions: [
          { key: 'edit', label: '编辑', onClick: enabled },
          { key: 'delete', label: '删除', disabled: true, onClick: disabled },
        ],
      },
      global,
    })

    expect(wrapper.text()).toContain('编辑')
    expect(wrapper.text()).toContain('删除')
    expect(wrapper.text()).toContain('更多')
    await wrapper.find('a').trigger('click')
    await wrapper.find('button').trigger('click')

    expect(enabled).toHaveBeenCalledOnce()
    expect(disabled).not.toHaveBeenCalled()
  })
})
