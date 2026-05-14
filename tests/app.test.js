const request = require('supertest');
const app = require('../src/app');

describe('Vip-Center Backend API', () => {
  it('responde health check', async () => {
    const response = await request(app).get('/api/health');

    expect(response.status).toBe(200);
    expect(response.body.status).toBe('ok');
  });

  it('lista planes', async () => {
    const response = await request(app).get('/api/plans');

    expect(response.status).toBe(200);
    expect(Array.isArray(response.body)).toBe(true);
    expect(response.body.length).toBeGreaterThan(0);
  });

  it('crea un nuevo miembro', async () => {
    const response = await request(app)
      .post('/api/members')
      .send({
        name: 'María López',
        email: 'maria@vipcenter.com',
        planId: 1,
      });

    expect(response.status).toBe(201);
    expect(response.body.name).toBe('María López');
    expect(response.body.planId).toBe(1);
  });

  it('registra un check-in para un miembro existente', async () => {
    const response = await request(app)
      .post('/api/checkins')
      .send({ memberId: 1 });

    expect(response.status).toBe(201);
    expect(response.body.memberId).toBe(1);
    expect(response.body.createdAt).toBeDefined();
  });
});
