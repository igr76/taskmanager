Запуск
скопируйте код например в intellij idea и запустите его

наберите в bash для создания задачи:  curl -X POST http://localhost:8080/api/tasks \
-H "Content-Type: application/json" \
-d '{"title":"Первая задача","description":"Тест"}'

Откройте в браузере:
http://localhost:8080/api/tasks?page=0&size=10

Получить задачу по ID
http://localhost:8080/api/tasks/1

обновить задачу

curl -X PATCH http://localhost:8080/api/tasks/1/status \
-H "Content-Type: application/json" \
-d '{"status":"DONE"}'

удалить задачу

curl -X DELETE http://localhost:8080/api/tasks/1