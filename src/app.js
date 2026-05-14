const express = require('express');

const app = express();
app.use(express.json());

const plans = [
  { id: 1, name: 'Básico', price: 80000 },
  { id: 2, name: 'Premium', price: 130000 },
];

const members = [
  { id: 1, name: 'Juan Pérez', email: 'juan@vipcenter.com', planId: 2 },
];

const checkins = [];

app.get('/api/health', (_req, res) => {
  res.json({ status: 'ok', service: 'Vip-Center Backend' });
});

app.get('/api/plans', (_req, res) => {
  res.json(plans);
});

app.get('/api/members', (_req, res) => {
  res.json(members);
});

app.post('/api/members', (req, res) => {
  const { name, email, planId } = req.body;

  if (!name || !email || !planId) {
    return res.status(400).json({ error: 'name, email y planId son obligatorios' });
  }

  const planExists = plans.some((plan) => plan.id === Number(planId));
  if (!planExists) {
    return res.status(400).json({ error: 'El plan no existe' });
  }

  const member = {
    id: members.length ? members[members.length - 1].id + 1 : 1,
    name,
    email,
    planId: Number(planId),
  };

  members.push(member);
  return res.status(201).json(member);
});

app.post('/api/checkins', (req, res) => {
  const { memberId } = req.body;

  if (!memberId) {
    return res.status(400).json({ error: 'memberId es obligatorio' });
  }

  const member = members.find((item) => item.id === Number(memberId));
  if (!member) {
    return res.status(404).json({ error: 'Miembro no encontrado' });
  }

  const checkin = {
    id: checkins.length ? checkins[checkins.length - 1].id + 1 : 1,
    memberId: member.id,
    createdAt: new Date().toISOString(),
  };

  checkins.push(checkin);
  return res.status(201).json(checkin);
});

app.get('/api/checkins', (_req, res) => {
  res.json(checkins);
});

module.exports = app;
