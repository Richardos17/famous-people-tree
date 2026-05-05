CREATE INDEX country_id IF NOT EXISTS
FOR (c:Country) ON (c.localId);