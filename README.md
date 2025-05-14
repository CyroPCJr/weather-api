# 🌦️ WeatherWorld API

Uma API RESTful desenvolvida com **Spring Boot + Kotlin**, que fornece informações meteorológicas por cidade, com
suporte a cache, rate limiting e integrações externas como a **OpenWeather API**.

---

## 🚀 Funcionalidades

### 🔍 Consulta de Clima

- [x] Buscar clima por cidade: `GET /weather?city=São Paulo`
- [ ] Buscar clima por coordenadas: `GET /weather?lat=-22.7&lon=-47.6`
- [ ] Buscar múltiplas cidades de uma vez: `GET /weather?cities=SP,RJ,BH`

### ⚡ Performance & Caching

- [x] Cache com `@Cacheable` para evitar chamadas repetidas à OpenWeather API
- [ ] Paginação e ordenação (para endpoints futuros como histórico)
- [x] Testes de cache verificados com Spring Boot + Mockk

### 🔒 Rate Limiting

- [x] Limite de requisições usando Bucket4k (1000 requisições/dia)
- [x] Retorno amigável em caso de limite atingido (`HTTP 429 Too Many Requests`)

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

## 🚀 Funcionalidades

* Consulta de clima atual por cidade
* Integração com OpenWeatherMap API
* Cache com Spring Cache (EhCache)
* Rate limiting com Bucket4j
* Suporte a múltiplas unidades de temperatura (Celsius/Fahrenheit)
* Tratamento global de exceções com mensagens amigáveis

---

## 📘 API Endpoints

| Método | Endpoint | Descrição                                                                                                              |
|--------|----------|------------------------------------------------------------------------------------------------------------------------|
| GET    | /weather | Retorna a previsão do tempo para uma cidade específica. Suporta cache, rate limit e customização de unidade de medida. |

---

### ☀️ GET /weather

Retorna os dados climáticos atuais de uma cidade.

#### 📥 Parâmetros de query:

| Nome  | Tipo            | Obrigatório | Padrão | Descrição                                                         |
|-------|-----------------|-------------|--------|-------------------------------------------------------------------|
| city  | String          | ✅ Sim       | —      | Nome da cidade a ser consultada                                   |
| units | TemperatureUnit | ❌ Não       | METRIC | Unidade de temperatura: METRIC (Celsius) ou IMPERIAL (Fahrenheit) |

---

#### ✅ Exemplo de resposta de sucesso (200 OK)

```
json
{
"name": "Piracicaba",
"main": {
"temp": 24.21,
"feelsLike": 24.41,
"tempMin": 24.21,
"tempMax": 24.21,
"pressure": 1023,
"humidity": 66
},
"weather": [
{
"main": "Clouds",
"description": "broken clouds",
"icon": "04d"
}
]
}
```

---

#### ⚠️ Respostas de erro

| Código | Motivo                | Exemplo de corpo da resposta                                         |
|--------|-----------------------|----------------------------------------------------------------------|
| 404    | Cidade não encontrada | { "cod": "404", "message": "City 'x' not found" }                    |
| 429    | Rate limit excedido   | { "cod": "429", "message": "Rate limit exceeded. Try again later." } |
| 500    | Erro interno da API   | { "cod": "500", "message": "Error calling weather API: ..." }        |

---

## 🛠️ Tecnologias utilizadas

* Kotlin
* Spring Boot
* Spring Cache
* Bucket4j
* Feign Client
* OpenWeatherMap API
* JUnit + MockK

---

## 🧪 Executando os testes

bash
./gradlew test


---

## 📆 Variáveis de ambiente

| Nome            | Descrição                      |
|-----------------|--------------------------------|
| WEATHER_API_KEY | Chave de API do OpenWeatherMap |

---

## 📄 Licença

Este projeto está sob a licença MIT.

## ⚙️ Como rodar localmente

```bash
git clone https://github.com/seu-usuario/weatherworld-api.git
cd weatherworld-api
./gradlew bootRun
