# Curl request collection

Go back to [Main README](../../README.md)

### NB:
Please note that initial request will have cold-start.

---
Examples for the Spectrum API (`https://spectrum-app-manual.onrender.com`). In PowerShell, use `curl.exe` (rather than the `curl` alias).

Set up the variables below in a Bash-like shell. Replace the IDs with values returned by your API. In PowerShell, set the same variables with `$env:BASE_URL = "https://spectrum-app-manual.onrender.com"` and so on, and replace `$BASE_URL` references with `$env:BASE_URL`.

```sh
export BASE_URL="https://spectrum-app-manual.onrender.com"
export HOTEL_ID="0136fd05-fba7-47c5-b54c-f927af873ff5"
export ROOM_ID="74f8ed09-36e8-4024-8d99-a790de8ae570"
```

## Authentication

`HttpOnly` prevents browser JavaScript from reading the `access_token` cookie; it does not prevent curl from storing or sending it. The cookie is also `Secure`, so use the HTTPS base URL above. Use curl's cookie jar (`-c` to save, `-b` to send) instead of copying the token manually.

```sh
curl -sS -c cookies.txt -w '\nHTTP %{http_code}\n' -X POST "$BASE_URL/v1/auth/register" \
  -H 'Content-Type: application/json' \
  -d '{"email":"api.tester@example.com","password":"qwerty1234567"}' #gitleaks:allow

# Log in with the account whose role is needed for the protected operation.
curl -sS -c cookies.txt -w '\nHTTP %{http_code}\n' -X POST "$BASE_URL/v1/auth/login" \
  -H 'Content-Type: application/json' \
  -d '{"email":"worker_test@example.com","password":"worker_testworker_testworker_test"}' #gitleaks:allow
```

New accounts have the `CLIENT` role. Hotel creation requires `WORKER` or `ADMIN`; room create/update requires `WORKER`. Use a provisioned worker/admin account for these calls. The login response saves its cookie to `cookies.txt`; pass `-b cookies.txt` on protected requests. Treat the cookie jar as a credential and remove it when you finish testing.

## Hotels

List hotels with filters, pagination, and sorting. Repeat `amenity` to request multiple amenities.

```sh
curl -i -G "$BASE_URL/v1/api/hotels" \
  --data-urlencode 'location=Kyiv' \
  --data-urlencode 'checkIn=2027-06-15' \
  --data-urlencode 'checkOut=2027-06-20' \
  --data-urlencode 'minPrice=100' \
  --data-urlencode 'maxPrice=300' \
  --data-urlencode 'minRating=3.5' \
  --data-urlencode 'maxRating=5' \
  --data-urlencode 'amenity=Wi-Fi' \
  --data-urlencode 'amenity=Pool' \
  --data-urlencode 'sortBy=RATING' \
  --data-urlencode 'sortOrder=DESC' \
  --data-urlencode 'page=0' \
  --data-urlencode 'limit=10'
```

Create a hotel (worker/admin cookie required):

```sh
curl -i -X POST "$BASE_URL/v1/api/hotels" \
  -H 'Content-Type: application/json' \
  -b cookies.txt \
  -d '{"name":"Grand Budapest","address":{"city":"Kyiv","district":"Podilskyi","street":"Skovorody","buildingNumber":34}}'
```

Fetch a hotel by ID:

```sh
curl -i "$BASE_URL/v1/api/hotels/$HOTEL_ID"
```

## Rooms

List rooms for a hotel. Repeat `amenity` for multiple amenity filters.

```sh
curl -i -G "$BASE_URL/v1/api/hotels/$HOTEL_ID/rooms" \
  --data-urlencode 'roomType=DOUBLE' \
  --data-urlencode 'roomStatus=ACTIVE' \
  --data-urlencode 'checkIn=2027-06-15' \
  --data-urlencode 'checkOut=2027-06-20' \
  --data-urlencode 'minPrice=100' \
  --data-urlencode 'maxPrice=300' \
  --data-urlencode 'amenity=Wi-Fi' \
  --data-urlencode 'sortBy=PRICE_PER_NIGHT' \
  --data-urlencode 'sortOrder=ASC' \
  --data-urlencode 'page=0' \
  --data-urlencode 'limit=10'
```

Create a room (worker cookie required):

```sh
curl -i -X POST "$BASE_URL/v1/api/hotels/$HOTEL_ID/rooms" \
  -H 'Content-Type: application/json' \
  -b cookies.txt \
  -d '{"roomNumber":"204","roomType":"DOUBLE","pricePerNight":{"amount":145.00,"currency":"USD"},"roomStatus":"ACTIVE","amenities":["Wi-Fi","TV"]}'
```

Fetch a room by ID:

```sh
curl -i "$BASE_URL/v1/api/hotels/$HOTEL_ID/rooms/$ROOM_ID"
```

Update room fields (worker cookie required). The request body may include only the fields being changed.

```sh
curl -i -X PUT "$BASE_URL/v1/api/hotels/$HOTEL_ID/rooms/$ROOM_ID" \
  -H 'Content-Type: application/json' \
  -b cookies.txt \
  -d '{"pricePerNight":{"amount":159.00,"currency":"USD"},"amenities":["Wi-Fi","TV","Mini-bar"]}'
```

## Validation and error-path examples

These requests should return validation errors (`400`), unauthorized (`401`), forbidden (`403`), conflict (`409`), or not found (`404`) as appropriate. They are useful for checking common failure responses.

```sh
# Invalid pagination value
curl -i -G "$BASE_URL/v1/api/hotels" --data-urlencode 'page=-1'

# Invalid body (hotel name too short)
curl -i -X POST "$BASE_URL/v1/api/hotels" \
  -H 'Content-Type: application/json' \
  -b cookies.txt \
  -d '{"name":"AB","address":{"city":"Kyiv","district":"Podilskyi","street":"Skovorody","buildingNumber":34}}'

# Missing/invalid credentials for a protected operation
curl -i -X POST "$BASE_URL/v1/api/hotels" \
  -H 'Content-Type: application/json' \
  -d '{"name":"Another Hotel","address":{"city":"Kyiv","district":"Podilskyi","street":"Skovorody","buildingNumber":34}}'

# Unknown hotel and room IDs
curl -i "$BASE_URL/v1/api/hotels/123e4567-e89b-12d3-a456-426614174999"
curl -i "$BASE_URL/v1/api/hotels/$HOTEL_ID/rooms/321e7564-e89b-12d3-a456-426614174999"

# Login with an unknown email
curl -i -X POST "$BASE_URL/v1/auth/login" \
  -H 'Content-Type: application/json' \
  -d '{"email":"nobody@example.com","password":"qwerty1234567"}' #gitleaks:allow
```

The API definition is in [openapi.yaml](openapi.yaml). Query values are case-sensitive enum values; the room sort order is `ASC` or `DESC`.
