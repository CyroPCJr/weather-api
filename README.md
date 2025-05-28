# 🌦️ WeatherWorld API

[![Kotlin](https://img.shields.io/badge/kotlin-2.1.21-blueviolet?logo=kotlin&logoColor=white)](https://kotlinlang.org)
![GitHub Workflow Status](https://img.shields.io/github/actions/workflow/status/CyroPCJr/Arms-App/android-ci.yml?label=Build&logo=github)
![GitHub License](https://img.shields.io/github/license/CyroPCJr/Arms-App)

Uma API RESTful desenvolvida com **Spring Boot + Kotlin**, que fornece informações meteorológicas por cidade, com
suporte a cache, rate limiting e integrações externas como a **OpenWeather API**.

---

## 🚀 Funcionalidades

### 🔍 Consulta de Clima

- [x] Buscar clima por cidade: `GET /weather?city=São Paulo`
- [x] Buscar clima por coordenadas: `GET /weather?lat=-22.7&lon=-47.6`
- [x] Buscar múltiplas cidades de uma vez: `GET /weather?cities=SP,RJ,BH`

### ⚡ Performance & Caching

- [x] Sistema de cache pra otimizar o uso da OpenWeather API
- [x] Testes de cache verificados com Spring Boot + Mockk

### 🔒 Rate Limiting

- [x] Limite de requisições usando Bucket4k (1000 requisições/dia)
- [x] Retorno amigável em caso de limite atingido (`HTTP 429 Too Many Requests`)

### 🌐 Integrações

- [x] Integração com a OpenWeather API
- [ ] Suporte a múltiplas APIs de clima (WeatherAPI, AccuWeather etc.)

### 🧪 Testes

- [x] Testes unitários com Mockk e Spring Boot
- [x] Testes de carga para avaliar caching e throttling

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

| Método | Endpoint                    | Descrição                                                                                                              |
|--------|-----------------------------|------------------------------------------------------------------------------------------------------------------------|
| GET    | /api/weather/by-city        | Retorna a previsão do tempo para uma cidade específica. Suporta cache, rate limit e customização de unidade de medida. |
| GET    | /api/weather/by-coordinates | Retorna a previsão do tempo para uma cidade específica. Suporta cache, rate limit e customização de unidade de medida. |

---

### ☀️ GET /weather

Retorna os dados climáticos atuais de uma cidade.

#### 📥 Parâmetros de query:

| Nome  | Tipo            | Obrigatório | Padrão | Descrição                                                         |
|-------|-----------------|-------------|--------|-------------------------------------------------------------------|
| city  | String          | ✅ Sim       | —      | Nome da cidade a ser consultada                                   |
| units | TemperatureUnit | ❌ Não       | METRIC | Unidade de temperatura: METRIC (Celsius) ou IMPERIAL (Fahrenheit) |

Retonar os dados climaticos atuais utilizando latitude e longitude

### 📥 Parâmetros de query:

| Nome  | Tipo            | Obrigatório | Padrão | Descrição                                                         |
|-------|-----------------|-------------|--------|-------------------------------------------------------------------|
| lat   | Double          | ✅ Sim       | —      | latitude                                                          |
| lon   | Double          | ✅ Sim       | —      | longitude                                                         |
| units | TemperatureUnit | ❌ Não       | METRIC | Unidade de temperatura: METRIC (Celsius) ou IMPERIAL (Fahrenheit) |

---

#### ✅ Exemplo de resposta de sucesso (200 OK)

```json
{
  "coord": {
    "lon": -46.6361,
    "lat": -23.5475
  },
  "weather": [
    {
      "id": 800,
      "main": "Clear",
      "description": "clear sky",
      "icon": "01d"
    }
  ],
  "base": "stations",
  "main": {
    "temp": 294.32,
    "feels_like": 295.05,
    "temp_min": 293.89,
    "temp_max": 294.51,
    "pressure": 1023,
    "humidity": 98,
    "sea_level": 1023,
    "grnd_level": 933
  },
  "visibility": 10000,
  "wind": {
    "speed": 4.06,
    "deg": 97,
    "gust": 5.04
  },
  "clouds": {
    "all": 0
  },
  "dt": 1747230002,
  "sys": {
    "type": 2,
    "id": 2041565,
    "country": "BR",
    "sunrise": 1747215182,
    "sunset": 1747254774
  },
  "timezone": -10800,
  "id": 3448439,
  "name": "São Paulo",
  "cod": 200
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

Este projeto está sob a [MIT License](LICENSE).

## ⚙️ Como rodar localmente

```bash
git clone https://github.com/seu-usuario/weatherworld-api.git
cd weatherworld-api
./gradlew bootRun
