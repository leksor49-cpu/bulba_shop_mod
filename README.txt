BULBA SHOP - NeoForge 1.21.1 (21.1.229)

КАК СОБРАТЬ БЕЗ УСТАНОВКИ ЧЕГО-ЛИБО (через GitHub):
1. Зарегистрируйся на github.com (бесплатно) и нажми New repository. Назови как хочешь, нажми Create.
2. На странице репозитория: "uploading an existing file". Перетащи туда ВСЁ содержимое этой папки
   (src, build.gradle, settings.gradle, gradle.properties и папку .github), нажми Commit changes.
   Если папка .github не загрузилась: Add file -> Create new file, имя .github/workflows/build.yml,
   и вставь туда содержимое файла build.yml из архива.
3. Открой вкладку Actions - там пойдёт сборка (3-5 минут). Дождись зелёной галочки.
4. Открой этот запуск, внизу в разделе Artifacts скачай bulbashop-jar (это zip, внутри файл bulbashop-1.0.0.jar).
5. Положи jar в папку mods (и клиента, и сервера - мод нужен на обеих сторонах).

В ИГРЕ:
- Открой инвентарь (E) - над окном кнопка "Магазин".
- Вкладка "Купить": все предметы сборки. Вкладка "Продать": твои вещи.
- Админам: /bulba add 1000 - выдать себе монет, /bulba balance - баланс.

НАСТРОЙКА ЦЕН: файл config/bulbashop-server.toml (или saves/<мир>/serverconfig/ в одиночке):
buyMarkup, sellRatio, craftMarkup и priceOverrides (ручные цены).
