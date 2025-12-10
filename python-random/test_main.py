import pytest
from main import app

@pytest.fixture
def client():
    app.config['TESTING'] = True
    with app.test_client() as client:
        yield client

def test_random_endpoint_no_header(client):
    """Test random endpoint without X-App-Name header."""
    response = client.get('/random')
    assert response.status_code == 403
    assert response.get_json() == {"error": "Access denied"}

def test_random_endpoint_with_header(client):
    """Test random endpoint with X-App-Name header."""
    headers = {
        'X-App-Name': 'getting-random'
    }
    response = client.get('/random', headers=headers)
    assert response.status_code == 200
    data = response.get_json()
    assert 'random_number' in data
    assert isinstance(data['random_number'], int)
    assert 1 <= data['random_number'] <= 100
