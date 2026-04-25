# Ordering System - Deployment Guide

## 🚀 Quick Start

### Prerequisites
- Docker & Docker Compose v2+
- Java 17+ (for local development)
- Maven 3.8+
- Git

### Local Development (No Docker)

1. Setup MySQL database
```bash
mysql -u root -p
CREATE DATABASE ordering_system;
CREATE USER 'ordering_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON ordering_system.* TO 'ordering_user'@'localhost';
FLUSH PRIVILEGES;
```

2. Configure environment (edit or create `.env` file)
```bash
cp .env.example .env
# Edit .env with your settings
```

3. Build and run
```bash
mvn clean package
java -jar target/ordering-system.jar
```

Access at: http://localhost:8080

---

## 🐳 Docker Deployment (Recommended)

### Option 1: Basic Deployment (Spring Boot only)
```bash
docker-compose up -d
```

### Option 2: Full Stack with Nginx (Production Recommended)
```bash
docker-compose -f docker-compose.nginx.yml up -d
```

Access at: http://localhost

### Docker Commands
```bash
# View logs
docker-compose logs -f app

# Stop services
docker-compose down

# Rebuild
docker-compose build --no-cache
docker-compose up -d

# Check health
docker-compose ps
```

---

## ☁️ Cloud Deployment

### AWS Elastic Beanstalk
```bash
# Build JAR
mvn clean package

# Initialize EB
eb init -p java-17 ordering-system --region us-east-1

# Create environment
eb create ordering-system-env

# Deploy
eb deploy
```

### Heroku
```bash
heroku create ordering-system
heroku addons:create heroku-postgresql:hobby-dev
heroku config:set SPRING_PROFILES_ACTIVE=prod
heroku plugins:install heroku-cli-deploy
heroku deploy:target/ordering-system.jar --app ordering-system
```

### Google Cloud Run
```bash
# Build container
gcloud builds submit --tag gcr.io/PROJECT-ID/ordering-system

# Deploy
gcloud run deploy ordering-system \
  --image gcr.io/PROJECT-ID/ordering-system \
  --platform managed \
  --region us-central1 \
  --allow-unauthenticated \
  --set-env-vars SPRING_PROFILES_ACTIVE=prod
```

### Azure App Service
```bash
# Create App Service Plan
az appservice plan create --name ordering-system-plan --resource-group myResourceGroup --sku B1 --is-linux

# Create Web App
az webapp create --resource-group myResourceGroup --plan ordering-system-plan --name ordering-system --runtime 'JAVA|17'

# Configure MySQL
az webapp config appsettings set --resource-group myResourceGroup --name ordering-system --settings SPRING_DATASOURCE_URL=jdbc:mysql://<mysql-server>.mysql.database.azure.com:3306/ordering_system SPRING_DATASOURCE_USERNAME=user@mysql-server SPRING_DATASOURCE_PASSWORD=password

# Deploy JAR
az webapp deploy --resource-group myResourceGroup --name ordering-system --src-path target/ordering-system.jar --type jar
```

---

## 🔐 Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `MYSQLHOST` | MySQL hostname | localhost |
| `MYSQLPORT` | MySQL port | 3306 |
| `MYSQLDATABASE` | Database name | ordering_system |
| `MYSQLUSER` | Database user | root |
| `MYSQLPASSWORD` | Database password | password |
| `SERVER_PORT` | Application port | 8080 |
| `ADMIN_USER` | Admin username | admin |
| `ADMIN_PASSWORD` | Admin password | admin123 |
| `SPRING_PROFILES_ACTIVE` | Spring profile | prod |

---

## 🔒 Production Security Checklist

- [ ] Change default admin credentials
- [ ] Enable SSL/TLS (use Let's Encrypt)
- [ ] Configure firewall rules (allow only 80/443)
- [ ] Set up database backups
- [ ] Enable access logging
- [ ] Configure rate limiting in Nginx
- [ ] Set up monitoring (Prometheus/Grafana)
- [ ] Enable HTTPS redirect
- [ ] Rotate database credentials
- [ ] Set up automated security updates

---

## 📊 Monitoring

### Health Check
```bash
curl http://localhost:8080/actuator/health
```

### Application Logs
```bash
# Docker
docker-compose logs -f app

# Direct
tail -f logs/application.log
```

---

## 🚦 Maintenance

### Database Migrations
```bash
# Update schema in src/main/resources/application.properties
spring.jpa.hibernate.ddl-auto=update  # DEV only
spring.jpa.hibernate.ddl-auto=validate  # PROD
```

### Backup Database (Docker)
```bash
docker-compose exec mysql mysqldump -u root -p$MYSQL_ROOT_PASSWORD ordering_system > backup.sql
```

### Restore Database
```bash
cat backup.sql | docker-compose exec -T mysql mysql -u root -p$MYSQL_ROOT_PASSWORD ordering_system
```

---

## 📈 Performance Tuning

### JVM Options (add to Dockerfile or environment)
```
JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
```

### Connection Pool (HikariCP)
```properties
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=20000
```

---

## 🔧 Troubleshooting

| Issue | Solution |
|-------|----------|
| Port 8080 already in use | Change `server.port` in application.properties |
| MySQL connection refused | Verify MySQL is running: `docker-compose ps mysql` |
| Out of memory | Increase JVM heap: `-Xmx1024m` |
| SSL certificate errors | Use proper certificates in nginx/ssl/ directory |
| Template not found | Check thymeleaf template location in src/main/resources/templates/ |
| Login fails | Check logs for authentication errors |

---

## 📦 Build Profiles

### Development (default)
```bash
mvn spring-boot:run
# Features: Hot reload, SQL logging enabled, no cache
```

### Production
```bash
mvn clean package -DskipTests
java -Dspring.profiles.active=prod -jar target/ordering-system.jar
# Features: Optimized, cached, secure
```

---

## 💾 Scaling

### Horizontal Scaling
1. Deploy multiple app instances
2. Use external session store (Redis)
3. Configure load balancer (Nginx/HAProxy)

### Database Scaling
1. Enable MySQL replication
2. Configure read replicas
3. Implement connection pooling

---

## 📞 Support

For issues or questions:
- Check logs: `docker-compose logs app`
- Verify configuration: Check application.properties
- Test connectivity: `curl -I http://localhost:8080`