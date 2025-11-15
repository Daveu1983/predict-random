const express = require('express');
const app = express();
const PORT = process.env.PORT || 8080;

app.use((req, res, next) => {
    const appName = req.get('X-App-Name'); 
    if (appName === 'getting-random') {
        return next();
    }
    res.status(403).send('Forbidden');
});

app.get('/random', (req, res) => {
    const random_number = Math.floor(Math.random() * 100) + 1;
    res.json({ random_number });
});

app.listen(PORT, () => {
    console.log(`Server is running on port ${PORT}`);
});