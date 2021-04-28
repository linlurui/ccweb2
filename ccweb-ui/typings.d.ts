declare module "*.vue" {
  import Vue from 'vue'
  export default Vue
}

declare module "*.json" {
  const value: any;
  export default value;
}

declare module "*.js" {
  const value: any;
  export default value;
}

declare module 'require' {
  const value: any;
  export default value;
}

declare module 'vue-pdf' {
  const value: any;
  export default value;
}

declare module 'mammoth' {
  const value: any;
  export default value;
}

declare module '__dirname' {
  const value: string;
  export default value;
}

declare module '*.svg'
declare module '*.png'
declare module '*.jpg'
declare module '*.jpeg'
declare module '*.gif'
declare module '*.bmp'
declare module '*.tiff'
declare module '*.png'
