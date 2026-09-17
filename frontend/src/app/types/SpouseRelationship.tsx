import { Relationship } from "./Relationship";

export type SpouseRelationship = Relationship & 
{
    marriageStartDate: Date,
    marriageEndDate: Date,
}