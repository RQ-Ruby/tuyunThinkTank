import { generateService } from '@umijs/openapi'

generateService({
  requestLibPath: "import request from '@/request'",
  schemaPath: process.env.OPENAPI_SCHEMA_PATH || 'http://127.0.0.1:8123/api/v2/api-docs',
  serversPath: './src',
})