package com.applidium.candycrushsolver.engine;

import java.util.ArrayList;
import java.util.List;

public class MoveFinder {

    private List<List<Sweet>> grid = new ArrayList<>();
    private final List<Move> moves = new ArrayList<>();
    private boolean moveAlreadyAdded;
    private boolean symmetricalMoveDone;
    private Move.Direction symmetricalDirection;
    private boolean couldBeAFiveSpecialMove;
    //2 booleans for bomb special move
    private boolean bombTwoVertical;
    private boolean bombTwoHorizontal;

    public MoveFinder(List<List<Sweet>> grid) {
        this.grid = grid;
    }

    public Move findMove() {
        findAllMoves();
        return findBestMove();
    }

    public void findAllMoves() {
        for (int i = 0; i < grid.size(); i++) {
            for (int j = 0; j < grid.get(i).size(); j++) {
                manageMove(i, j);
            }
        }
    }

    private void manageMove(int i, int j) {
        reinitializeSymmetricalBooleans();
        //does the bottom cell exists ?
        if (canGoDown(i, j)) {
            checkDirectionDown(grid.get(i).get(j), i + 1, j);
            checkDirectionUp(grid.get(i + 1).get(j), i, j);
        }
        reinitializeSymmetricalBooleans();
        //does the right cell exists ?
        if (canGoRight(i, j)) {
            checkDirectionRight(grid.get(i).get(j), i, j + 1);
            checkDirectionLeft(grid.get(i).get(j + 1), i, j);
        }
    }

    private void reinitializeSymmetricalBooleans() {
        symmetricalMoveDone = false;
        symmetricalDirection = null;
    }

    private void checkDirectionUp(Sweet reference, int i, int j) {
        initializeBooleansToFindSpecialMoves();
        checkMatchUpUp(reference, i, j);
        checkMatchVerticalLeft(reference, i, j);
        checkMatchVerticalRight(reference, i, j);
    }

    private void checkMatchVerticalRight(Sweet reference, int i, int j) {
        boolean couldBeFourVerticalMove = false;
        if (sameColorOnYourRight(reference, i, j)) {
            if (sameColorOnYourRight(reference, i, j + 1)) {
                if (!moveAlreadyAdded && !symmetricalMoveDone) {
                    addThisMove(reference, grid.get(i).get(j));
                    couldBeFourVerticalMove = true;
                } else if (couldBeAFiveSpecialMove && moveAlreadyAdded) {
                    moves.get(moves.size() - 1).addToScore(3);
                } else if (bombTwoVertical && moveAlreadyAdded) {
                    moves.get(moves.size() - 1).addToScore(4);
                }
                if (symmetricalMoveDone && !moveAlreadyAdded && symmetricalDirection == Move.Direction.DOWN) {
                    moves.get(moves.size() - 1).addToScore(1);
                }
            }
            if (sameColorOnYourLeft(reference, i, j)) {
                if (!moveAlreadyAdded && !symmetricalMoveDone) {
                    addThisMove(reference, grid.get(i).get(j));
                } else if (couldBeFourVerticalMove && moveAlreadyAdded) {
                    moves.get(moves.size() - 1).addToScore(3);
                } else if (bombTwoVertical && moveAlreadyAdded) {
                    moves.get(moves.size() - 1).addToScore(4);
                }
                if (symmetricalMoveDone && !moveAlreadyAdded && symmetricalDirection == Move.Direction.DOWN) {
                    moves.get(moves.size() - 1).addToScore(1);
                }
            }
        }
    }

    private void checkMatchVerticalLeft(Sweet reference, int i, int j) {
        boolean couldBeFourVerticalMove = false;
        if (sameColorOnYourLeft(reference, i, j)) {
            if (sameColorOnYourLeft(reference, i, j - 1)) {
                if (!moveAlreadyAdded && !symmetricalMoveDone) {
                    addThisMove(reference, grid.get(i).get(j));
                    couldBeFourVerticalMove = true;
                    couldBeAFiveSpecialMove = true;
                } else if (bombTwoVertical && moveAlreadyAdded) {
                    moves.get(moves.size() - 1).addToScore(4);
                }
                if (symmetricalMoveDone && !moveAlreadyAdded && symmetricalDirection == Move.Direction.DOWN) {
                    moves.get(moves.size() - 1).addToScore(1);
                }
            }
        }
        if (sameColorOnYourRight(reference, i, j)) {
            if (couldBeFourVerticalMove && moveAlreadyAdded) {
                moves.get(moves.size() - 1).addToScore(3);
            }
        }
    }

    private void checkMatchUpUp(Sweet reference, int i, int j) {
        if (sameColorAbove(reference, i, j)) {
            if (sameColorAbove(reference, i - 1, j) && !symmetricalMoveDone) {
                addThisMove(reference, grid.get(i).get(j));
                bombTwoVertical = true;
            }
            if (sameColorAbove(reference, i - 1, j) && symmetricalMoveDone && !moveAlreadyAdded && symmetricalDirection == Move.Direction.DOWN) {
                moves.get(moves.size() - 1).addToScore(1);
            }
        }
    }

    private void checkDirectionDown(Sweet reference, int i, int j) {
        initializeBooleansToFindSpecialMoves();
        checkMatchDownDown(reference, i, j);
        checkMatchVerticalLeft(reference, i, j);
        checkMatchVerticalRight(reference, i, j);
    }

    private void checkMatchDownDown(Sweet reference, int i, int j) {
        if (sameColorUnder(reference, i, j)) {
            if (sameColorUnder(reference, i + 1, j)) {
                addThisMove(reference, grid.get(i).get(j));
                bombTwoVertical = true;
            }
        }
    }

    private void checkDirectionLeft(Sweet reference, int i, int j) {
        initializeBooleansToFindSpecialMoves();
        checkMatchLeftLeft(reference, i, j);
        checkMatchHorizontalUp(reference, i, j);
        checkMatchHorizontalDown(reference, i, j);
    }

    private void checkMatchHorizontalDown(Sweet reference, int i, int j) {
        boolean couldBeFourHorizontalMove = false;
        if (sameColorUnder(reference, i, j)) {
            if (sameColorUnder(reference, i + 1, j)) {
                if (!moveAlreadyAdded && !symmetricalMoveDone) {
                    addThisMove(reference, grid.get(i).get(j));
                    couldBeFourHorizontalMove = true;
                } else if (couldBeAFiveSpecialMove && moveAlreadyAdded) {
                    moves.get(moves.size() - 1).addToScore(4);
                } else if (bombTwoHorizontal && moveAlreadyAdded) {
                    moves.get(moves.size() - 1).addToScore(4);
                }
                if (symmetricalMoveDone && !moveAlreadyAdded && symmetricalDirection == Move.Direction.RIGHT) {
                    moves.get(moves.size() - 1).addToScore(1);
                }
            }
            if (sameColorAbove(reference, i, j)) {
                if (!moveAlreadyAdded && !symmetricalMoveDone) {
                    addThisMove(reference, grid.get(i).get(j));
                } else if (couldBeFourHorizontalMove && moveAlreadyAdded) {
                    moves.get(moves.size() - 1).addToScore(2);
                } else if (bombTwoHorizontal && moveAlreadyAdded) {
                    moves.get(moves.size() - 1).addToScore(4);
                }
                if (symmetricalMoveDone && !moveAlreadyAdded && symmetricalDirection == Move.Direction.RIGHT) {
                    moves.get(moves.size() - 1).addToScore(1);
                }
            }
        }
    }

    private void checkMatchHorizontalUp(Sweet reference, int i, int j) {
        boolean couldBeFourHorizontalMove = false;
        couldBeAFiveSpecialMove = false;
        if (sameColorAbove(reference, i, j)) {
            if (sameColorAbove(reference, i - 1, j)) {
                if (!moveAlreadyAdded && !symmetricalMoveDone) {
                    addThisMove(reference, grid.get(i).get(j));
                    couldBeFourHorizontalMove = true;
                    couldBeAFiveSpecialMove = true;
                } else if (bombTwoVertical && moveAlreadyAdded) {
                    moves.get(moves.size() - 1).addToScore(4);
                }
                if (symmetricalMoveDone && !moveAlreadyAdded && symmetricalDirection == Move.Direction.RIGHT) {
                    moves.get(moves.size() - 1).addToScore(1);
                }
            }
        }
        if (sameColorUnder(reference, i, j)) {
            if (couldBeFourHorizontalMove && moveAlreadyAdded) {
                moves.get(moves.size() - 1).addToScore(2);
            }
        }
    }

    private void checkMatchLeftLeft(Sweet reference, int i, int j) {
        if (sameColorOnYourLeft(reference, i, j)) {
            if (sameColorOnYourLeft(reference, i, j - 1) && !symmetricalMoveDone) {
                addThisMove(reference, grid.get(i).get(j));
                bombTwoHorizontal = true;
            }
            if (sameColorOnYourLeft(reference, i, j - 1) && symmetricalMoveDone && !moveAlreadyAdded && symmetricalDirection == Move.Direction.RIGHT) {
                moves.get(moves.size() - 1).addToScore(1);
            }
        }
    }

    private void checkDirectionRight(Sweet reference, int i, int j) {
        initializeBooleansToFindSpecialMoves();
        checkMatchRightRight(reference, i, j);
        checkMatchHorizontalUp(reference, i, j);
        checkMatchHorizontalDown(reference, i, j);
    }

    private void checkMatchRightRight(Sweet reference, int i, int j) {
        if (sameColorOnYourRight(reference, i, j)) {
            if (sameColorOnYourRight(reference, i, j + 1)) {
                addThisMove(reference, grid.get(i).get(j));
                bombTwoHorizontal = true;
            }
        }
    }

    private void addThisMove(Sweet reference, Sweet other) {
        if (reference == null || other == null || reference.getType() == other.getType()) {
            return;
        }
        Move move = new Move(reference, other, 1); //score : added later
        moves.add(move);
        moveAlreadyAdded = true;
        symmetricalMoveDone = true;
        symmetricalDirection = move.findDirection();
    }

    private boolean canGoUp(int i, int j) {
        return i != 0 && grid.get(i - 1) != null && j < grid.get(i - 1).size() && grid.get(i - 1).get(j) != null;
    }

    private boolean canGoDown(int i, int j) {
        return i < grid.size() - 1 && grid.get(i + 1) != null && j < grid.get(i + 1).size() && grid.get(i + 1).get(j) != null;
    }

    private boolean canGoLeft(int i, int j) {
        return j != 0 && j - 1 < grid.get(i).size() && grid.get(i).get(j - 1) != null;
    }

    private boolean canGoRight(int i, int j) {
        return j < grid.get(i).size() - 1 && grid.get(i).get(j + 1) != null;
    }

    private boolean sameColor(Sweet reference, Sweet other) {
        if (reference == null || other == null) {
            return false;
        }
        return reference.getType() == other.getType();
    }

    private boolean sameColorOnYourRight(Sweet reference, int i, int j) {
        if (canGoRight(i, j)) {
            if (sameColor(reference, grid.get(i).get(j + 1))) {
                return true;
            }
        }
        return false;
    }

    private boolean sameColorOnYourLeft(Sweet reference, int i, int j) {
        if (canGoLeft(i, j)) {
            if (sameColor(reference, grid.get(i).get(j - 1))) {
                return true;
            }
        }
        return false;
    }

    private boolean sameColorAbove(Sweet reference, int i, int j) {
        if (canGoUp(i, j)) {
            if (sameColor(reference, grid.get(i - 1).get(j))) {
                return true;
            }
        }
        return false;
    }

    private boolean sameColorUnder(Sweet reference, int i, int j) {
        if (canGoDown(i, j)) {
            if (sameColor(reference, grid.get(i + 1).get(j))) {
                return true;
            }
        }
        return false;
    }

    private void initializeBooleansToFindSpecialMoves() {
        moveAlreadyAdded = false;
        couldBeAFiveSpecialMove = false;
        bombTwoHorizontal = false;
        bombTwoVertical = false;
    }

    private Move findBestMove() {
        Move bestMove = null;
        int bestScore = 0;
        for (int i = 0; i < moves.size(); i++) {
            if (moves.get(i).getScore() >= bestScore) {
                bestMove = moves.get(i);
                bestScore = moves.get(i).getScore();
            }
        }
        return bestMove;
    }

    public List<Move> getMoves() {
        return moves;
    }
}
