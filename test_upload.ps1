# 测试图片上传到空间的脚本
# 这个脚本用于验证修复后的spaceId设置是否正确

Write-Host "开始测试图片上传到空间功能..." -ForegroundColor Green

# 空间ID（无敌倩倩的空间）
$spaceId = "1971836291768381442"

Write-Host "空间ID: $spaceId" -ForegroundColor Yellow

# 检查后端服务器是否运行
Write-Host "检查后端服务器状态..." -ForegroundColor Blue
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8123/api/health" -Method GET -TimeoutSec 5 -ErrorAction Stop
    Write-Host "后端服务器运行正常" -ForegroundColor Green
} catch {
    Write-Host "后端服务器未运行或无法访问" -ForegroundColor Red
    Write-Host "请确保后端服务器在 http://localhost:8123 运行" -ForegroundColor Red
    exit 1
}

Write-Host "`n测试完成！" -ForegroundColor Green
Write-Host "请在前端页面 http://localhost:5174 进行以下操作：" -ForegroundColor Yellow
Write-Host "1. 登录系统" -ForegroundColor White
Write-Host "2. 进入'我的空间'页面" -ForegroundColor White
Write-Host "3. 点击进入'无敌倩倩的空间'" -ForegroundColor White
Write-Host "4. 上传一张新图片" -ForegroundColor White
Write-Host "5. 验证图片是否显示在空间中" -ForegroundColor White

Write-Host "`n然后运行以下命令检查数据库：" -ForegroundColor Yellow
Write-Host "mysql -u root -p123456 -h localhost -P 3306 -D tuyun -e `"SELECT id, name, spaceId, createTime FROM picture ORDER BY createTime DESC LIMIT 3;`"" -ForegroundColor Cyan