// .eslintrc.js
module.exports = {
  root: true,
  env: {
    node: true,
    browser: true,
    es2021: true
  },
  extends: [
    'plugin:vue/vue3-essential',
    'eslint:recommended'
  ],
  parserOptions: {
    ecmaVersion: 2021,
    sourceType: 'module',
    requireConfigFile: false
  },
  globals: {
    // Vue 3 编译器宏
    defineProps: 'readonly',
    defineEmits: 'readonly',
    defineExpose: 'readonly',
    withDefaults: 'readonly',
    defineOptions: 'readonly',
    defineSlots: 'readonly'
  },
  rules: {
    'no-console': 'off',
    'no-debugger': 'off'
  }
}