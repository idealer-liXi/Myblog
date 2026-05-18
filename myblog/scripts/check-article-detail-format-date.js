const fs = require('fs')
const path = require('path')

const filePath = path.resolve(__dirname, '../src/components/ArticleDetail.vue')
const source = fs.readFileSync(filePath, 'utf8')

const templateMatch = source.match(/<template>([\s\S]*?)<\/template>/)
const scriptSetupMatch = source.match(/<script setup>([\s\S]*?)<\/script>/)

if (!templateMatch || !scriptSetupMatch) {
  console.error('ArticleDetail.vue is missing a <template> or <script setup> block.')
  process.exit(1)
}

const template = templateMatch[1]
const scriptSetup = scriptSetupMatch[1]
const usesFormatDate = /formatDate\s*\(/.test(template)
const definesFormatDate = /(const|let|var)\s+formatDate\s*=|function\s+formatDate\s*\(/.test(scriptSetup)

if (usesFormatDate && !definesFormatDate) {
  console.error('ArticleDetail.vue template uses formatDate(), but <script setup> does not define it.')
  process.exit(1)
}

console.log('ArticleDetail.vue formatDate check passed.')
