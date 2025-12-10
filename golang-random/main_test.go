package main

import (
    "encoding/json"
    "net/http"
    "net/http/httptest"
    "testing"
)

func TestRandomHandler(t *testing.T) {
    t.Run("should return 403 if X-App-Name is not getting-random", func(t *testing.T) {
        req, err := http.NewRequest("GET", "/random", nil)
        if err != nil {
            t.Fatal(err)
        }

        rr := httptest.NewRecorder()
        handler := http.HandlerFunc(randomHandler)

        handler.ServeHTTP(rr, req)

        if status := rr.Code; status != http.StatusForbidden {
            t.Errorf("handler returned wrong status code: got %v want %v",
                status, http.StatusForbidden)
        }
    })

    t.Run("should return a random number if X-App-Name is getting-random", func(t *testing.T) {
        req, err := http.NewRequest("GET", "/random", nil)
        if err != nil {
            t.Fatal(err)
        }
        req.Header.Set("X-App-Name", "getting-random")

        rr := httptest.NewRecorder()
        handler := http.HandlerFunc(randomHandler)

        handler.ServeHTTP(rr, req)

        if status := rr.Code; status != http.StatusOK {
            t.Errorf("handler returned wrong status code: got %v want %v",
                status, http.StatusOK)
        }

        var target map[string]int
        err = json.Unmarshal(rr.Body.Bytes(), &target)
        if err != nil {
            t.Fatal(err)
        }

        if val, ok := target["random_number"]; !ok {
            t.Errorf("random_number not found in response")
        } else {
            if val < 1 || val > 100 {
                t.Errorf("random_number is not in range 1-100")
            }
        }
    })
}
