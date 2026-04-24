CREATE INDEX person_name IF NOT EXISTS
FOR (p:Person) ON (p.name);

CREATE INDEX country_name IF NOT EXISTS
FOR (c:Country) ON (c.name);