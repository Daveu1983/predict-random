package main
import (
	"encoding/json"
	"math/rand"
	"net/http"
	"os"
	"time"
)

func randomHandler(w http.ResponseWriter, r *http.Request) {
	if r.Header.Get("X-App-Name") != "getting-random" {
		http.Error(w, "Forbidden", http.StatusForbidden)
		return
	}

	randomNumber := rand.Intn(100) + 1
	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(map[string]int{"random_number": randomNumber})
}

func main() {
	rand.Seed(time.Now().UnixNano())

	http.HandleFunc("/random", randomHandler)

	port := os.Getenv("PORT")
	if port == "" {
		port = "8081"
	}
	http.ListenAndServe(":"+port, nil)
}