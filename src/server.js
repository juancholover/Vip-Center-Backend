const app = require('./app');

const port = process.env.PORT || 3000;

app.listen(port, () => {
  console.log(`Vip-Center Backend escuchando en el puerto ${port}`);
});
