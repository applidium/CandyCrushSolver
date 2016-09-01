package com.applidium.candycrushsolver.engine;

public class Move {
    public enum Direction { UP, DOWN, LEFT, RIGHT } //the direction is seen as sweet1 point of view

    private final Sweet sweet1;
    private final Sweet sweet2;
    private Direction direction;

    /* CALCULATING SCORES (not real score in the game, personal score to fond the best move) :
    1 : plain move
    2 : move that can make other moves with falling sweets (or +1 point if the move is better than 1)
    3 : create a 4-special horizontal sweet (with horizontal stripes)
    4 : create a 4-special vertical sweet (with vertical stripes)
    5 : create a special bomb sweet
    7 : create a 5-special sweet
    */
    private int score;

    public Move(Sweet sweet1, Sweet sweet2, int score, Direction direction) {
        this.sweet1 = sweet1;
        this.sweet2 = sweet2;
        this.score = score;
        this.direction = direction;
    }

    public Sweet getSweet1() {
        return sweet1;
    }

    public Sweet getSweet2() {
        return sweet2;
    }

    public int getScore() {
        return score;
    }

    public Direction getDirection() {
        return direction;
    }

    public void addToScore(int nb) {
        score += nb;
    }
}
