package Models.Game;

import Models.Grid.Grid;
import Models.Grid.SquareStatus;
import Models.Player.Player;
import Models.Move.PlayerMove;
import Models.Player.PlayerStatus;

import java.io.Serializable;
import java.util.List;

public class NormalGame extends Game implements Serializable {


    // Constructors
    public NormalGame(List ListOfPlayers){
        super(ListOfPlayers);
        currentRules=new DefaultRules();
        this.whenHitMine = currentRules.getPressMineBehavior();
        this.whenScoreNegative = currentRules.getScoreNegativeBehavior();
        this.points = currentRules.getPoints();
    }
    public NormalGame(int Width,int Height,int NumMines,List ListOfPlayers){
        super(Width,Height,NumMines,ListOfPlayers);
        currentRules=new DefaultRules();
        this.whenHitMine = currentRules.getPressMineBehavior();
        this.whenScoreNegative = currentRules.getScoreNegativeBehavior();
        this.points = currentRules.getPoints();
    }
    public NormalGame(int width, int height, int numMines, List players,
                      Points points, WhenHitMine pressMineBehavior, WhenScoreNegative scoreNegativeBehavior) {
        super(width, height, numMines, players);
        currentRules=new DefaultRules(points,pressMineBehavior,scoreNegativeBehavior);
        this.whenHitMine = pressMineBehavior;
        this.whenScoreNegative = scoreNegativeBehavior;
        this.points = points;
    }



    // InnerClass
    // #see GameRules In Game Class
    public class DefaultRules extends GameRules implements Serializable {
        public Points points;
        protected WhenHitMine PressMineBehavior;
        protected WhenScoreNegative ScoreNegativeBehavior;

        public DefaultRules() {
            PressMineBehavior = WhenHitMine.Lose;
            ScoreNegativeBehavior=WhenScoreNegative.End;
            points = new Points();
        }
        public DefaultRules(WhenHitMine pressMineBehavior, WhenScoreNegative scoreNegativeBehavior) {
            PressMineBehavior = pressMineBehavior;
            ScoreNegativeBehavior=scoreNegativeBehavior;
            points = new Points();
        }
        public DefaultRules(Points _points, WhenHitMine pressMineBehavior, WhenScoreNegative scoreNegativeBehavior) {
            points=_points;
            PressMineBehavior=pressMineBehavior;
            ScoreNegativeBehavior=scoreNegativeBehavior;

        }

        @Override
        protected void ChangePlayerStatus(List<PlayerMove> moves) {
            if(moves.size()==0){
                currentPlayer.setCurrentStatus(PlayerStatus.waiting);
                return;
            }
            if (moves.get(0).getSquare().getStatus() == SquareStatus.OpenedMine && PressMineBehavior == WhenHitMine.Lose){
                currentPlayer.setCurrentStatus(PlayerStatus.Lose);
                return;
            }
            if( currentPlayer.getCurrentScore().getScore() < 0
                    && PressMineBehavior == WhenHitMine.Continue
                    && ScoreNegativeBehavior ==WhenScoreNegative.End){
                currentPlayer.setCurrentStatus(PlayerStatus.Lose);
                return;
            }
            currentPlayer.setCurrentStatus(PlayerStatus.waiting);
        }

        @Override
        protected void GetScoreChange(List<PlayerMove> moves) {
            if(moves.size()==0){
                return;
            }
            if (moves.size() == 1) {
                PlayerMove move = moves.get(0);
                switch (move.getSquare().getStatus()) {
                    case OpenedEmpty:
                        points.addRevealEmptyPoints(currentPlayer);
                        break;
                    case OpenedNumber:
                        currentPlayer.getCurrentScore().addPoints(move.getSquare().getNumberOfSurroundedMines());
                        break;
                    case OpenedMine:
                            if(PressMineBehavior!=WhenHitMine.Lose)
                                points.addRevealMinePoints(currentPlayer);
                        break;
                    case Marked:
//                        move.getSquare().isMine() ? points.addMarkMinePoints() : points.addMarkNotMinePoints();
                        if (move.getSquare().isMine()) { points.addMarkMinePoints(currentPlayer); }
                        else { points.addMarkNotMinePoints(currentPlayer); }
                        break;
                    case Closed: // This case is when user unmark marked sqaure so it will be closed;
                        if(move.getSquare().isMine()){ points.addUnmarkMinePoints(currentPlayer); }
                        else{ points.addUnmarkNotMinePoints(currentPlayer); }
                        break;
                }
                return;
            }
            else {//In this case .. More than one sqaure revealed so >>
                points.addRevealFloodFill(currentPlayer,moves.size());
            }
        }

        @Override
        public void DecideNextPlayer(List<PlayerMove> moves) {
            if(Replay!=GameReplay.on){
                if(moves.size()!=0) {
                    GameMoves.add(moves.get(0));
                }
                else{
                    PlayerMove move=new PlayerMove();
                    if(status!=GameStatus.Finish)
                        GameMoves.add(move);
                }
            }
            currentRules.GetScoreChange(moves);
            currentRules.ChangePlayerStatus(moves);
            if(status!=GameStatus.Finish) {
                ChangeStatus();
                int indOfcurrentPlayer = players.lastIndexOf(currentPlayer);
                for (int i = 0; i < players.size(); i++) {
                    indOfcurrentPlayer = (indOfcurrentPlayer + 1) % players.size();
                    if (players.get(indOfcurrentPlayer).getCurrentStatus() == PlayerStatus.waiting) {
                        setCurrentPlayer(players.get(indOfcurrentPlayer));
                        return;
                    }
                }
                setCurrentPlayer(currentPlayer);
            }
        }

        public Points getPoints() {
            return points;
        }

        public WhenHitMine getPressMineBehavior() {
            return PressMineBehavior;
        }

        public WhenScoreNegative getScoreNegativeBehavior() {
            return ScoreNegativeBehavior;
        }

        public void setPoints(Points points) {
            this.points = points;
        }

        public void setPressMineBehavior(WhenHitMine pressMineBehavior) {
            PressMineBehavior = pressMineBehavior;
        }

        public void setScoreNegativeBehavior(WhenScoreNegative scoreNegativeBehavior) {
            ScoreNegativeBehavior = scoreNegativeBehavior;
        }
    }
    private WhenHitMine whenHitMine ;
    private WhenScoreNegative whenScoreNegative ;
    private Points points;

    public WhenHitMine getWhenHitMine() {
        return whenHitMine;
    }

    public Points getPoints() {
        return points;
    }

    public WhenScoreNegative getWhenScoreNegative() {
        return whenScoreNegative;
    }

    @Override
    public void StartGame() {};

    public void GetMove(){};
    protected void UpdateVeiw(List<PlayerMove> Moves){};
    protected void EndGame(){};
}
