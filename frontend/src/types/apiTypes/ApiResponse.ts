import type { Person } from "../Person";
import type { Relationship } from "../Relationship";
import { PersonApi } from "./PersonApi";
import { RelationshipApi } from "./RelationshipApi";

export type ApiResponse = {
  persons: PersonApi[];
  relationships: RelationshipApi[];
};