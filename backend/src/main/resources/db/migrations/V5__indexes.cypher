CREATE INDEX country_name IF NOT EXISTS
FOR (c:Country) ON (c.name);