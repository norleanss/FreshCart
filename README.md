# FreshCart

Учебное приложение продуктового магазина: каталог с поиском, фильтром по категориям и бесконечной прокруткой, карточка товара и корзина со скидками.

Бизнес-логика и работа с сетью вынесены в **общий Kotlin Multiplatform модуль**, Android-интерфейс написан на **Jetpack Compose**. Данные берутся из публичного REST API [dummyjson.com](https://dummyjson.com).

## Стек

| Слой | Технологии |
|---|---|
| Общий модуль (`shared`) | Kotlin Multiplatform (Android + iOS), Ktor Client, kotlinx.serialization, Coroutines / StateFlow |
| Android (`androidApp`) | Jetpack Compose, Material 3, Navigation Compose, ViewModel, Coil |
| Тесты | kotlin.test, Ktor MockEngine, kotlinx-coroutines-test, JUnit 4 |
| Сборка | Gradle Kotlin DSL, Version Catalog |

## Архитектура

```
shared/                        ← общий код для Android и iOS
  model/      Product, Category, Money (суммы в центах, без ошибок Double)
  data/       ProductRepository, маппинг DTO → доменные модели
  data/remote ProductApi (Ktor), expect/actual HTTP-движок (OkHttp / Darwin)
  cart/       Cart (иммутабельная корзина: суммы, скидка, лимит остатка), CartRepository (StateFlow)
  di/         SharedModule: ручной DI

androidApp/                    ← только UI и состояние экранов
  ui/catalog  CatalogScreen + CatalogViewModel (поиск с debounce, категории, пагинация)
  ui/details  ProductDetailsScreen (галерея на HorizontalPager)
  ui/cart     CartScreen (изменение количества, итог, экономия)
```

Корзина является единственным источником истины. `CartRepository` отдаёт `StateFlow<Cart>`, и все экраны подписаны на него, поэтому счётчик на карточке, бейдж в шапке и итог корзины всегда совпадают.

## Что покрыто тестами

- `shared/commonTest`: деньги и округление скидок, правила корзины (лимит остатка, удаление при нуле, итоги и экономия), репозиторий на Ktor `MockEngine` (выбор эндпоинта, пагинация, обработка ошибок сервера, кеш категорий).
- `androidApp/src/test`: тесты `CatalogViewModel` (первая загрузка, догрузка страниц, debounce поиска, переключение категорий, ошибка сети и повтор, синхронизация с корзиной).

## Запуск

1. Android Studio Ladybug (2024.2) или новее, JDK 17.
2. `File → Open` → папка проекта, дождаться синхронизации Gradle.
3. Запустить конфигурацию `androidApp` на эмуляторе или устройстве (minSdk 24).

Тесты:

```bash
./gradlew :shared:testDebugUnitTest :androidApp:testDebugUnitTest
```

iOS-таргеты объявлены в `shared` и собираются на macOS; iOS-интерфейса в проекте пока нет.
