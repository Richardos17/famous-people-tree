import type { Person } from "../Person";
import type { Relationship } from "../Relationship";
import { PersonApi } from "./PersonApi";
import { RelationshipApi } from "./RelationshipApi";

export type ApiTreeResponse = {
  persons: PersonApi[];
  relationships: RelationshipApi[];
};