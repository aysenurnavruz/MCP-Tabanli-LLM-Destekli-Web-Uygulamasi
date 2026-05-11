# Docker ile Calistirma

Projeyi tek komutla ayaga kaldirmak icin Docker Desktop acik olmali.

```bash
docker compose up --build
```

Servisler:

- Frontend: http://localhost:5173
- Backend: http://localhost:8080
- AI service MCP: http://localhost:8081/mcp
- MySQL: localhost:3306
- Qdrant: http://localhost:6333

Arka planda calistirmak icin:

```bash
docker compose up --build -d
```

Loglari izlemek icin:

```bash
docker compose logs -f
```

Durdurmak icin:

```bash
docker compose down
```

Veritabani verisini de sifirlamak icin:

```bash
docker compose down -v
```

OpenAI key dosyaya yazilmadi. Gerektiginde calistirmadan once terminalde environment olarak verilebilir:

```bash
export OPENAI_API_KEY="sk-..."
docker compose up --build
```
