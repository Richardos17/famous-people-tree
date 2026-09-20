import { PersonConnectedEdges } from "@/app/types/PersonConnectedEdges";
import { FamilyGraph } from "@/types/FamilyGraph";
import { FamilyTreeLayout } from "@/types/FamilyTreeLayout";
import { Person } from "@/types/Person";
import { type Node, type Edge, MarkerType } from "@xyflow/react";
import {
  calculateParentPosition,
  calculateChildPosition,
  calculateMarriagePosition
} from "./positionCalculator";
import { HandlePosition } from "@/app/types/HandlePosition";
import { EdgeType } from "./edgeType";
import RelationshipEdge from "@/components/RelationshipEdge";

export function layoutFamilyTree(
  graph?: FamilyGraph,
  centerNodeId?: string,
): FamilyTreeLayout {
  const personEdges: Map<string, PersonConnectedEdges> = new Map();
  const nodes: Map<string, Node> = new Map();
  const edges: Edge[] = [];
  //create layout with nodes
  if (graph === undefined) throw new Error("Graph is undefined");
  if (centerNodeId === undefined) throw new Error("CenterNodeId is undefined");
  const centerPerson: Person | undefined = graph.persons.get(centerNodeId);
  if (centerPerson === undefined)
    throw new Error("Center person id not found in graph.");
  nodes.set(centerPerson.wikidataId, {
    id: centerPerson.wikidataId,
    data: {
      person: centerPerson,
      handlePosition: {
        top: false,
        bottom: false,
        right: false,
        left: false,
      },
    },
    position: { x: 0, y: 0 },
    type: "person",
  });
  personEdges.set(centerPerson.wikidataId, {
    top: 0,
    bottom: 0,
    left: 0,
    right: 0,
  });
  const queue: string[] = [centerNodeId];
  const visited = new Set<string>([centerNodeId]);
  while (queue.length > 0) {
    const current: string = queue.shift()!;
    visited.add(current!);
    const currentNode = nodes.get(current!);
    const currentEdgesNumber = personEdges.get(current!);
    if (currentNode === undefined) throw new Error("Current node is undefined");
    if (currentEdgesNumber === undefined)
      throw new Error("Current edges is undefined");
    graph.relationships
      .filter(
        (relationship) =>
          ((relationship.fromWikidataId === current ||
            relationship.toWikidataId === current) &&
            relationship.type === EdgeType.Parent) ||
          (relationship.fromWikidataId === current &&
            relationship.type === EdgeType.Spouse),
      )
      .forEach((relationship) => {
        const currentIsParent = relationship.fromWikidataId === current;

        const otherPersonId = currentIsParent
          ? relationship.toWikidataId
          : relationship.fromWikidataId;

        if (visited.has(otherPersonId)) {
          return;
        }
        const newPerson: Person | undefined = graph.persons.get(otherPersonId);
        if (newPerson === undefined)
          throw new Error("Person connected by edge is missing.");
        if (relationship.type === EdgeType.Spouse) {
            nodes.set(newPerson.wikidataId, {
              id: newPerson.wikidataId,
              data: {
                person: newPerson,
                handlePosition: {
                  top: false,
                  bottom: false,
                  right: false,
                  left: true,
                },
              },
              position: calculateMarriagePosition(
                currentNode.position,
                currentEdgesNumber,
              ),
              type: "person",
            });
            updateHandlePosition(currentNode, {
              right: true,
            });
            edges.push({
              data: { edgeType: EdgeType.Spouse, marriageStartDate: relationship.marriageStartDate, marriageEndDate: relationship.marriageEndDate },
              id: current + "-" + newPerson.wikidataId,
              source: current,
              sourceHandle: "r" + current,
              target: newPerson.wikidataId,
              targetHandle: "l" + newPerson.wikidataId,
              type: "parent",
              markerEnd: { type: MarkerType.ArrowClosed },
            });
            personEdges.set(newPerson.wikidataId, {
              top: 0,
              bottom: 0,
              left: 1,
              right: 0,
            });
            currentEdgesNumber.right += 1;
            queue.push(otherPersonId);
        } else {
          if (currentIsParent) {
            nodes.set(newPerson.wikidataId, {
              id: newPerson.wikidataId,
              data: {
                person: newPerson,
                handlePosition: {
                  top: false,
                  bottom: true,
                  right: false,
                  left: false,
                },
              },
              position: calculateParentPosition(
                currentNode.position,
                currentEdgesNumber,
              ),
              type: "person",
            });
            updateHandlePosition(currentNode, {
              top: true,
            });
            edges.push({
              data: { edgeType: EdgeType.Parent },
              id: current + "-" + newPerson.wikidataId,
              source: current,
              sourceHandle: "t" + current,
              target: newPerson.wikidataId,
              targetHandle: "b" + newPerson.wikidataId,
              type: "parent",
              markerEnd: { type: MarkerType.ArrowClosed },
            });
            personEdges.set(newPerson.wikidataId, {
              top: 0,
              bottom: 1,
              left: 0,
              right: 0,
            });
            currentEdgesNumber.top += 1;
            queue.push(otherPersonId);
          } else {
            nodes.set(newPerson.wikidataId, {
              id: newPerson.wikidataId,
              data: {
                person: newPerson,
                handlePosition: {
                  top: true,
                  bottom: false,
                  right: false,
                  left: false,
                },
              },
              position: calculateChildPosition(
                currentNode.position,
                currentEdgesNumber,
              ),
              type: "person",
            });
            updateHandlePosition(currentNode, {
              bottom: true,
            });
            edges.push({
              data: { edgeType: EdgeType.Parent },
              id: newPerson.wikidataId + "-" + otherPersonId,
              source: newPerson.wikidataId,
              sourceHandle: "t" + newPerson.wikidataId,
              target: current,
              targetHandle: "b" + current,
              type: "parent",
              markerEnd: { type: MarkerType.ArrowClosed },
            });
            personEdges.set(newPerson.wikidataId, {
              top: 1,
              bottom: 0,
              left: 0,
              right: 0,
            });

            currentEdgesNumber.bottom += 1;
            queue.push(relationship.fromWikidataId);
          }
        }
      });
  }
  return { nodes: nodes.values().toArray(), edges: edges };
}
function updateHandlePosition(
  node: Node | undefined,
  options: {
    top?: boolean;
    bottom?: boolean;
    left?: boolean;
    right?: boolean;
  },
) {
  if (node === undefined) throw new Error("Selected node is undefined");
  const { top, bottom, left, right } = options;
  const handlePosition: HandlePosition = node.data.handlePosition;

  if (top != undefined) {
    handlePosition.top = top;
  }
  if (bottom != undefined) {
    handlePosition.bottom = bottom;
  }
  if (right != undefined) {
    handlePosition.right = right;
  }
  if (left != undefined) {
    handlePosition.left = left;
  }
}
