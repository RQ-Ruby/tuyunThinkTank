/// <reference types="vite/client" />

declare module '*.vue' {
  import type { DefineComponent } from 'vue'

  const component: DefineComponent<Record<string, unknown>, Record<string, unknown>, unknown>
  export default component
}

declare module 'vue-cropper' {
  import type { Plugin } from 'vue'

  const VueCropper: Plugin
  export default VueCropper
}

interface ImportMetaEnv {
  readonly VITE_API_ORIGIN?: string
  readonly VITE_DEMO_ADMIN_PASSWORD?: string
  readonly VITE_DEMO_CREATOR_PASSWORD?: string
  readonly VITE_DEMO_PHOTOGRAPHER_PASSWORD?: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
