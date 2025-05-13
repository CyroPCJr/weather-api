# 🌦️ WeatherWorld API

Uma API RESTful desenvolvida com **Spring Boot + Kotlin**, que fornece informações meteorológicas por cidade, com suporte a cache, rate limiting e integrações externas como a **OpenWeather API**.

---

## 🚀 Funcionalidades

### 🔍 Consulta de Clima
- [x] Buscar clima por cidade: `GET /weather?city=Piracicaba`
- [ ] Buscar clima por coordenadas: `GET /weather?lat=-22.7&lon=-47.6`
- [ ] Buscar múltiplas cidades de uma vez: `GET /weather?cities=SP,RJ,BH`

### ⚡ Performance & Caching
- [x] Cache com `@Cacheable` para evitar chamadas repetidas à OpenWeather API
- [ ] Paginação e ordenação (para endpoints futuros como histórico)
- [x] Testes de cache verificados com Spring Boot + Mockk

### 🔒 Rate Limiting
- [x] Limite de requisições usando Bucket4k (1000 requisições/dia)
- [ ] Retorno amigável em caso de limite atingido (`HTTP 429 Too Many Requests`)

### 🌐 Integrações
- [x] Integração com a OpenWeather API
- [ ] Suporte a múltiplas APIs de clima (WeatherAPI, AccuWeather etc.)
- [ ] Integração com Firebase (para log ou realtime)

### 🧪 Testes
- [x] Testes unitários com Mockk e Spring Boot
- [ ] Testes de integração reais com contextos carregados
- [ ] Testes de carga para avaliar caching e throttling

### 📊 Histórico e Tendências
- [ ] Armazenar histórico de consultas
- [ ] Permitir consulta de clima passado (via OpenWeather Historical ou cache interno)

### 🧠 Inteligência
- [ ] Recomendação automática (ex: “Leve guarda-chuva”)
- [ ] Classificação de clima em categorias ("ensolarado", "instável", "chuva leve"...)

---

## 🛠️ Tecnologias

- Kotlin + Spring Boot
- Spring Cache + CacheManager
- Bucket4k (Rate Limiting)
- OpenWeather API
- Gradle Kotlin DSL
- JUnit 5 + Mockk
- Docker (planejado)
- GitHub Actions (CI/CD planejado)

---

## ⚙️ Como rodar localmente

```bash
git clone https://github.com/seu-usuario/weatherworld-api.git
cd weatherworld-api
./gradlew bootRun
