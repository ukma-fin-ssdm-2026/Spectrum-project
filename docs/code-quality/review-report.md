[Reviewed repo](https://github.com/ukma-fin-ssdm-2026/Monkey-Business-project)

for commit `89123a2`

### Strong sides:
1. API is restful
2. No duplications
3. Small functions with single responsibilities


### Upgrades:
1. For endpoint /v1/boards/{board_id}/cards in [openapi spec](https://github.com/ukma-fin-ssdm-2026/Monkey-Business-project/blob/89123a2d3f51ef5ced7b3ecfc648680e1eadaed8/docs/api/openapi-generated.yaml#L80) 404 error has the same structure as validation error
2. For endpoint /v1/boards/{board_id}/cards in [openapi spec](https://github.com/ukma-fin-ssdm-2026/Monkey-Business-project/blob/89123a2d3f51ef5ced7b3ecfc648680e1eadaed8/docs/api/openapi-generated.yaml#L843) there are no examples for errors
3. Add validation for input data(e.g. [create_card](https://github.com/ukma-fin-ssdm-2026/Monkey-Business-project/blob/89123a2d3f51ef5ced7b3ecfc648680e1eadaed8/src/api/routers/cards.py#L103))


### Risk:
No auth (see [src/api](https://github.com/ukma-fin-ssdm-2026/Monkey-Business-project/tree/89123a2d3f51ef5ced7b3ecfc648680e1eadaed8/src/api))


#### AI usage

Initial investigation was done with LLM.