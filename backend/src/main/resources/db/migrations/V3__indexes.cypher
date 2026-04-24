CREATE INDEX person_name IF NOT EXISTS
FOR (p:Person) ON (p.name);