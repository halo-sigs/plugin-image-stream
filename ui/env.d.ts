/// <reference types="vite/client" />

export {}

declare module '*.vue' {
  import Vue from 'vue'
  export default Vue
}

declare module 'axios' {
  export interface AxiosRequestConfig {
    mute?: boolean
  }
}
