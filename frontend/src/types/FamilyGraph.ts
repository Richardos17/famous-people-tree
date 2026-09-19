import type { Person } from "./Person";
import type { Relationship } from "./Relationship";
export type FamilyGraph = {
  persons: Map<string, Person>;
  relationships: Relationship[];
};