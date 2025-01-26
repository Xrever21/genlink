const express = require('express');
const bodyParser = require('body-parser');
const { v4: uuidv4 } = require('uuid');
const fs = require('fs');

const app = express();
const PORT = 3002;

// Хранилище данных (замените на базу данных для продакшн)
const DB_FILE = './db.json';
let db = { users: {}, links: {} };

// Загрузка данных при старте сервера
if (fs.existsSync(DB_FILE)) {
  db = JSON.parse(fs.readFileSync(DB_FILE));
}

// Middleware
app.use(bodyParser.json());

// Сохранение данных
const saveDb = () => fs.writeFileSync(DB_FILE, JSON.stringify(db, null, 2));

// Создание короткой ссылки
app.post('/shorten', (req, res) => {
  const { longUrl, uuid } = req.body;

  if (!longUrl || !uuid) {
    return res.status(400).json({ error: 'URL and UUID are required.' });
  }

  const shortId = uuidv4().slice(0, 8);
  const shortUrl = `http://localhost:${PORT}/${shortId}`;

  db.links[shortId] = { longUrl, uuid, clicks: 0 };
  saveDb();

  res.json({ shortUrl });
});

// Редирект по короткой ссылке
app.get('/:shortId', (req, res) => {
  const { shortId } = req.params;
  const link = db.links[shortId];

  if (!link) {
    return res.status(404).send('Link not found.');
  }

  link.clicks += 1;
  saveDb();
  res.redirect(link.longUrl);
});

// Получение статистики
app.get('/stats/:shortId', (req, res) => {
  const { shortId } = req.params;
  const link = db.links[shortId];

  if (!link) {
    return res.status(404).json({ error: 'Link not found.' });
  }

  res.json({
    longUrl: link.longUrl,
    clicks: link.clicks,
  });
});

// Запуск сервера
app.listen(PORT, () => {
  console.log(`Backend running on http://localhost:${PORT}`);
});
