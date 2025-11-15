package main
import (
	"encoding/json"
	"math/rand"
	"net/http"
	"os"
	"time"
)

func main() {
	rand.Seed(time.Now().UnixNano())

	http.HandleFunc("/random", func(w http.ResponseWriter, r *http.Request) {
		if r.Header.Get("X-App-Name") != "getting-random" {
			http.Error(w, "Forbidden", http.StatusForbidden)
			return
		}

		randomNumber := rand.Intn(100) + 1
		w.Header().Set("Content-Type", "application/json")
		json.NewEncoder(w).Encode(map[string]int{"random_number": randomNumber})
	})

port := os.Getenv("PORT")
	if port == "" {
		port = "8080"
	}
	http.ListenAndServe(":"+port, nil)
}