# Docker Setup - SBA301 Computer Shop

## 🚀 Quick Start

### 1. Start SQL Server
```bash
docker-compose up -d
```

### 2. Initialize Database
**⚠️ Important:** SQL Server container doesn't auto-run init scripts. Run manually:

```powershell
# Option 1: Use helper script (Recommended)
.\docker\init-database.ps1

# Option 2: Run sqlcmd directly
sqlcmd -S localhost,1433 -U sa -P "Sa@12345" -i ".\docker\init-db\init.sql"
```

### 3. Verify Database
```powershell
# Check tables
sqlcmd -S localhost,1433 -U sa -P "Sa@12345" -Q "USE ComputerShopDB; SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE = 'BASE TABLE'"
```

### 4. Check status
```bash
docker-compose ps
docker-compose logs -f sqlserver
```

### 3. Stop and remove
```bash
# Keep data
docker-compose down

# Remove all data (fresh start)
docker-compose down -v
```

## 📊 Database Info

- **Host**: localhost
- **Port**: 1433
- **Database**: ComputerShopDB
- **Username**: sa
- **Password**: Sa@12345

## 👥 Default Users

| Username | Password | Role |
|----------|----------|------|
| admin | Admin@123 | ADMIN |
| user1 | Admin@123 | USER |
| staff1 | Admin@123 | STAFF |

## 📦 Sample Data

- **10 Products** (CPUs, GPUs, Peripherals)
- **16 Categories** (CPU, GPU, RAM, SSD, etc.)
- **10 Brands** (Intel, AMD, NVIDIA, ASUS, etc.)
- **10 Attributes** (Core Count, Memory Size, etc.)
- **3 Promotions** with product associations

## 🔧 Application Configuration

Update `application-local.yml`:
```yaml
spring:
  datasource:
    url: jdbc:sqlserver://localhost:1433;databaseName=ComputerShopDB;encrypt=true;trustServerCertificate=true
    username: sa
    password: Sa@12345
```

## 🔍 Troubleshooting

### Container is unhealthy
The healthcheck might show unhealthy initially. This is normal - wait for SQL Server to start, then run the init script.

### Connection refused
Wait 10-20 seconds after `docker compose up` for SQL Server to fully start, then run `.\docker\init-database.ps1`

### Init script not working
Make sure you have `sqlcmd` installed on your host machine. It comes with SQL Server or can be installed separately:
- [Download SQL Server Command Line Tools](https://learn.microsoft.com/en-us/sql/tools/sqlcmd/sqlcmd-utility)

### Reset database completely
```bash
# Remove all data and start fresh
docker compose down -v
docker compose up -d
.\docker\init-database.ps1
```

### Check database was created
```powershell
sqlcmd -S localhost,1433 -U sa -P "Sa@12345" -Q "SELECT name FROM sys.databases"
```
