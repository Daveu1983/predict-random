const request = require('supertest');
const app = require('./app');

describe('GET /random', () => {
  it('should return 403 if X-App-Name is not getting-random', async () => {
    const res = await request(app).get('/random');
    expect(res.statusCode).toEqual(403);
  });

  it('should return a random number if X-App-Name is getting-random', async () => {
    const res = await request(app)
      .get('/random')
      .set('X-App-Name', 'getting-random');
    expect(res.statusCode).toEqual(200);
    expect(res.body).toHaveProperty('random_number');
    expect(typeof res.body.random_number).toBe('number');
    expect(res.body.random_number).toBeGreaterThanOrEqual(1);
    expect(res.body.random_number).toBeLessThanOrEqual(100);
  });
});
