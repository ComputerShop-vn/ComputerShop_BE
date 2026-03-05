#!/bin/bash
# Start SQL Server in background
/opt/mssql/bin/sqlservr &
SQLSERVER_PID=$!

echo "Waiting for SQL Server to start..."
for i in $(seq 1 60); do
    /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "$SA_PASSWORD" -Q "SELECT 1" -b -C > /dev/null 2>&1
    if [ $? -eq 0 ]; then
        echo "SQL Server is ready. Running init script..."
        /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "$SA_PASSWORD" -i /docker-entrypoint-initdb.d/init.sql -b -C
        echo "Init script completed."
        break
    fi
    echo "Attempt $i/60 - SQL Server not ready yet..."
    sleep 2
done

# Keep container alive by waiting for SQL Server process
wait $SQLSERVER_PID
