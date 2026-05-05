CREATE INDEX person_id IF NOT EXISTS
FOR (p:Person) ON (p.localId);