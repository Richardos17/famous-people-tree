import { FamilyGraph } from "@/types/FamilyGraph";

export type PositionNode = {
  x: number;
  y: number;
  mod: number;
  shift: number;
  wikidataId: string;
  finalx: number;
};
export type FinalPositionNode = {
  x: number;
  y: number;
  wikidataId: string;
};
type SimpleEdge = { fromWikidataId: string; toWikidataId: string };

type PositionTree = { positionNodes: FinalPositionNode[]; edges: SimpleEdge[] };
export default function createPositionTree(
  familyGraph: FamilyGraph,
  startNode: string,
  directionUpwards: boolean,
): PositionTree {
  const adjList = preprocessEdges(familyGraph);
  firstPass(adjList, startNode, 0, directionUpwards);
  const children = adjList.get(startNode) || [];
  const rootNode: FinalPositionNode = { x: 0, y: 0, wikidataId: startNode };
  let rootNodeMod = 0;
  if (children.length > 0) {
    const existing = adjList.get(startNode);
    if (existing !== undefined)
      rootNodeMod =
        rootNode.x -
        (existing[0].x +
          existing[0].shift +
          (existing[existing.length - 1].x +
            existing[existing.length - 1].shift)) /
          2;
  }
  secondPass(adjList, startNode, 0, rootNodeMod + rootNode.x);
  const positionNodeList: FinalPositionNode[] = adjList
    .values()
    .toArray()
    .flat()
    .map((node) => ({
      x: node.finalx,
      y: node.y,
      wikidataId: node.wikidataId,
    }));
  const edges: SimpleEdge[] = familyGraph.relationships.map((relationship) => ({
    fromWikidataId: relationship.fromWikidataId,
    toWikidataId: relationship.toWikidataId,
  }));
  return {
    positionNodes: positionNodeList,
    edges: edges /* maybe not needed */,
  };
}
function preprocessEdges(
  familyGraph: FamilyGraph,
): Map<string, PositionNode[]> {
  const edgeMap: Map<string, PositionNode[]> = new Map();
  familyGraph.relationships.forEach((relationship) => {
    if (!edgeMap.has(relationship.fromWikidataId))
      edgeMap.set(relationship.fromWikidataId, []);
    edgeMap.get(relationship.fromWikidataId)?.push({
      wikidataId: relationship.toWikidataId,
      x: 0,
      y: 0,
      mod: 0,
      shift: 0,
      finalx: 0,
    });
  });
  return edgeMap;
}
const siblingDist: number = 1;
const verticalDist: number = 20;
const subTreeDist: number = 1;
type RecursionData = {
  childrenMidpoint: number;
  rightmostNodeST: Countour;
  leftmostNodeCurrentST: Countour;
};
export function firstPass(
  adjList: Map<string, PositionNode[]>,
  startNode: string,
  level: number,
  directionUpwards: boolean
): RecursionData {
  const children = adjList.get(startNode) || [];
  let firstNodeX: number = 0;
  let xPos: number = firstNodeX;
  const rightmostNodeContours: Countour[] = []; //used to save rightmost element maps of all children
  const leftmostNodeST: Countour = new Countour(); //stores leftmost node's of current node
  children.forEach((node, i) => {
    //iterate over all children of current node
    let leftmostNodeCurrentST: Countour = new Countour(); //current child's leftmost children
    if (
      adjList.has(node.wikidataId) &&
      adjList.get(node.wikidataId)!.length > 0 //if has children
    ) {
      const recursionData = firstPass(adjList, node.wikidataId, level + 1, directionUpwards); //post order traversal of child's children
      rightmostNodeContours.push(recursionData.rightmostNodeST);
      leftmostNodeCurrentST = recursionData.leftmostNodeCurrentST; //this are leftmost nodes only of the current child
      if (i == 0) {
        firstNodeX = recursionData.childrenMidpoint;
        xPos = firstNodeX;
      } else node.mod = xPos - recursionData.childrenMidpoint;
      leftmostNodeCurrentST.set(level, xPos); //include also the child into the leftmost nodes, maybe unnecesarry
      rightmostNodeContours[rightmostNodeContours.length - 1].set(level, xPos);
    } else {
      if (i == 0) {
        leftmostNodeST.set(level, xPos);
      }
      rightmostNodeContours.push(new Countour(level, xPos)); //because it's leaf, we need to create new map
    }
    node.x = xPos;
    node.y = level * verticalDist * (directionUpwards ? -1 : 1);
    xPos += siblingDist;

    if (i == 0) return; //cant deduce shifting when it's the first left child

    //apply shifts which we're caused by previous nodes
    leftmostNodeCurrentST.shift(node.shift, level, node.mod);
    //this one didn't exist before, when applying at the bottom
    rightmostNodeContours[rightmostNodeContours.length - 1].shift(
      node.shift,
      level,
      node.mod,
    );
    leftmostNodeST.merge(leftmostNodeCurrentST, true);
    let shiftNeeded = 0;
    let leftSubtreeIndex = -1; //index of most conflicting sibling
    for (let j = 0; j < i; j++) {
      const xDistances: number[] = leftmostNodeCurrentST.calculateDistances(
        rightmostNodeContours[j],
      );
      if (
        subTreeDist > Math.min(...xDistances) &&
        subTreeDist - Math.min(...xDistances) > shiftNeeded
      ) {
        shiftNeeded = subTreeDist - Math.min(...xDistances);
        leftSubtreeIndex = j;
      }
    }
    const shiftCurrent = relativeShift(shiftNeeded, leftSubtreeIndex, i);
    for (let j = 0; j < children?.length; j++) {
      children[j].shift += (shiftCurrent * j) / i;
      rightmostNodeContours[j]?.shift((shiftCurrent * j) / i); //apply shifts to siblings subtrees
    }
  });
  const rightmostNodeSTMap = new Countour(); //map of only the rightmost nodes for each level
  rightmostNodeContours.forEach((contour) => {
    rightmostNodeSTMap.merge(contour);
  });
  return {
    childrenMidpoint: (xPos - siblingDist + firstNodeX) / 2,
    rightmostNodeST: rightmostNodeSTMap,
    leftmostNodeCurrentST: leftmostNodeST,
  };
}
export function secondPass(
  adjList: Map<string, PositionNode[]>,
  startNode: string,
  modAcc: number,
  shiftAcc: number,
) {
  const children = adjList.get(startNode) || [];

  children.forEach((node) => {
    node.finalx = node.x + node.shift + shiftAcc + modAcc;
    secondPass(
      adjList,
      node.wikidataId,
      modAcc + node.mod,
      shiftAcc + node.shift,
    );
  });
}
function relativeShift(
  shiftNeeded: number,
  indexOfLeftElement: number,
  indexOfRightElement: number,
): number {
  return (
    Math.round(
      (100 * shiftNeeded) / (1 - indexOfLeftElement / indexOfRightElement),
    ) / 100
  );
}
class Countour {
  private levelPositionMap: Map<number, number>;
  public constructor();
  public constructor(level: number, xPos: number);
  public constructor(level?: number, xPos?: number) {
    this.levelPositionMap = new Map();
    if (level !== undefined && xPos !== undefined) {
      this.levelPositionMap.set(level, xPos);
    }
  }
  public set(level: number, xPos: number) {
    this.levelPositionMap.set(level, xPos);
  }
  public shift(shift: number): void;
  public shift(shift: number, currentLevel: number, mod: number): void;
  public shift(shift: number, currentLevel?: number, mod?: number) {
    if (currentLevel != undefined && mod != undefined)
      this.levelPositionMap.forEach((value, key, map) =>
        map.set(key, value + shift + (key > currentLevel ? mod : 0)),
      );
    else
      this.levelPositionMap.forEach((value, key, map) =>
        map.set(key, value + shift),
      );
  }
  public merge(other: Countour, keepLeft?: boolean) {
    //TODO extract
    other.levelPositionMap.forEach((value, key) => {
      const existing = this.levelPositionMap.get(key);
      if (keepLeft) {
        if (existing === undefined || existing > value) {
          this.levelPositionMap.set(key, value);
        }
      } else if (existing === undefined || existing < value)
        this.levelPositionMap.set(key, value);
    });
  }
  public calculateDistances(otherRightMost: Countour): number[] {
    const xDistances: number[] = [];

    this.levelPositionMap.forEach((value, key) => {
      const existing = otherRightMost.levelPositionMap.get(key);
      if (existing !== undefined) {
        xDistances.push(value - existing);
      }
    });
    return xDistances;
  }
}
