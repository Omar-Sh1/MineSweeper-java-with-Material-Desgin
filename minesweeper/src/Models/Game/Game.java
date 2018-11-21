/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Models.Game;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import BaseAlphabit.Converter;
import CustomSequences.SquareType2DArray;
import Models.Grid.Grid;
import Models.Grid.Square;
import Models.Grid.SquareStatus;
import Models.Move.MoveType;
import Models.Player.Player;
import Models.Move.PlayerMove;
import Models.Player.PlayerStatus;
import MineSweeperGameDefineException.IllegalBoundsOfGrid;
import MineSweeperGameDefineException.IllegalGameMove;

public abstract class Game {
    // <__ INNER CLASS __> \\
    public abstract class Timer extends Thread {
        protected int currentTime;
        public Timer() {
            this.currentTime = 10;
        }
        public Timer(int t) {
            this.currentTime = t;
        }

        public void run()
        {
            while (currentTime > 0) {
                Show(currentTime--);
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    // TODO: Some Handling way
                    //System.err.println("Interrupted Timer");
                    return;
                }
            }
            EndTimer();
        }
        public void setCurrentTime(int time){
            currentTime=time;
        }
        public abstract void Show(int Time);
        public abstract void EndTimer();
        public int getCurrentTime() {
            return currentTime;
        }
    }
    protected Timer currentTimer;
    public abstract class GameRules{
        protected abstract void ChangePlayerStatus(List<PlayerMove> moves);
        protected abstract void GetScoreChange(List<PlayerMove> moves);
        public abstract void DecideNextPlayer(List<PlayerMove> moves);
    }
    protected GameRules currentRules;
    // <__ DATA MEMBERS __> \\
    protected Player currentPlayer;
    protected Grid grid;
    protected GameStatus status;

    protected List<Player> players=new ArrayList<Player>();
    protected List<PlayerMove> moves=new ArrayList<PlayerMove>();

    // For View
    protected int FlagsNumber;
    protected int ShildNumber;
    protected int HeroShieldNumber;

    public Game(List _players){
        this(10,10,10,0,_players);
    }
    public Game(int Width,int Height,int NumMines,List _players){
        this(Width,Height,NumMines,0,_players);
    }
    public Game(int Width,int Height,int NumMines,int ShildCount,List _players){
        for(Object curPlayer:_players) {// add Players To the Game
            this.AddPlayer((Player) curPlayer);
        }
        if(!(_players.isEmpty()))
            setCurrentPlayer(players.get(0));

        initGame(Width,Height,NumMines,ShildCount);
    }
    // <__ METHODS __> \\
    protected void initGame(int width, int height, int minesCount,int ShildCount){
        try {
            grid=new Grid(width,height,minesCount,ShildNumber,HeroShieldNumber);
        } catch (IllegalBoundsOfGrid e) {
            e.handle();
            return;
        }setCurrentPlayer(players.get(0));
        this.status=GameStatus.FirstMove;// need to change to New Start game
        FlagsNumber = minesCount;
        ShildNumber = ShildCount;
    }
    protected void initGame(PlayerMove move){
        try {
            grid=new Grid(this.grid.getWidth()-1,this.grid.getHeight()-1,this.grid.getMinesCount(),ShildNumber,HeroShieldNumber,move);
        } catch (IllegalBoundsOfGrid e) {
            e.handle();
            return;
        }
        status=GameStatus.Running;
    }
    protected void ApplyPlayerMove(PlayerMove move) {
        // here We ApPly The move And then Check The Status Of The Game And Players
        if(status==GameStatus.FirstMove){
            initGame(move);
        }
        moves=this.grid.AcceptMove(move);
        currentTimer.interrupt();
        currentRules.DecideNextPlayer(moves);
        if(move.getType()==MoveType.Mark){
            FlagsNumber +=(move.getSquare().getStatus()==SquareStatus.Marked ?-1:1);
        }
    }
    protected void AcceptMove(PlayerMove move)throws IllegalGameMove {// x Rows Y columns
        Square s = move.getSquare();
        if(status==GameStatus.FirstMove){
            ApplyPlayerMove(move);
            return;
        }
        if(SquareType2DArray.CheckIndex(s.getX(),s.getY(),grid.getWidth(),grid.getHeight()))
        {
            move.setSquare(grid.getField()[move.getSquare().getX()][move.getSquare().getY()]);
            if(move.getType()==MoveType.Reveal) {
                if (move.getSquare().getStatus() == SquareStatus.Closed) {
                    ApplyPlayerMove(move);
                    return;
                }
                else{
                    throw new IllegalGameMove("Not Close Square");
                }
            }
            else{
                if(move.getSquare().getStatus() == SquareStatus.Marked  || (FlagsNumber >0 && move.getSquare().getStatus()==SquareStatus.Closed)) {
                    ApplyPlayerMove(move);
                    return;
                }
                else{
                    throw new IllegalGameMove("flags more than mines");
                }
            }
        }
        throw new IllegalGameMove("Out Of Bounds");
    }
    protected void ChangeStatus(){

        Square[][] feild =this.grid.getField();
        int num=0;
        for(int i=1;i<this.grid.getHeight();i++){
            for(int j=1;j<this.grid.getWidth();j++){
                if(status==GameStatus.FirstMove)continue;
                switch (feild[i][j].getStatus()){
                    case OpenedMine:
                    case Marked:
                    case Closed:
                        num++;
                        break;
                }
            }
        }
        boolean CanContinue=false;
        for(int i=0;i<players.size();i++){
            if(players.get(i).getCurrentStatus()!= PlayerStatus.Lose){
                CanContinue=true;
            }
        }
        if(status==GameStatus.Finish || num==this.grid.getMinesCount() || !CanContinue){
            status=GameStatus.Finish;
        }
        else if(moves.size()!=0){
                status=GameStatus.Running;
        }
    }
    protected void AddPlayer(Player player)
    {
        players.add(player);
    }

    // <__ SETTERS-GETTERS __> \\
    //Setters
    protected void setCurrentPlayer(Player currentPlayer) {
        this.currentPlayer = currentPlayer;
        if(currentPlayer.getCurrentStatus()!=PlayerStatus.Lose) {
            currentPlayer.setCurrentStatus(PlayerStatus.Playing);
            currentPlayer.Play();
        }
    }
    protected void setStatus(GameStatus status) {
        this.status = status;
    }
    //Getters
    public GameStatus getStatus() {
        return status;
    }

    //This func Implement in each kind of game Like Console Or GUI...
    public abstract void StartGame();
    protected abstract void GetMove();
    protected abstract void EndGame();
    protected abstract void UpdateVeiw(List<PlayerMove> Moves);

    // This Function for Debug
    public void PrintGrid() {
        System.out.print("   ");
        for(int i=0;i+1<this.grid.getWidth();i++){
            System.out.print(" "+ Converter.valueOf(i));
        }
        System.out.println();
        Square[][] feild=this.grid.getField();
        for(int i=1;i<this.grid.getHeight();i++){
            System.out.print("\n");
            System.out.print(" "+i+"  ");
            for (int j=1;j<this.grid.getWidth();j++){
                if(!feild[i][j].isMine())
                    System.out.print(feild[i][j].getNumberOfSurroundedMines()+" ");
                else System.out.print("B ");
            }
        }
        System.out.println();
    }
    public static String fixedLengthString(String string, int length) {
        return String.format("%1$"+length+ "s  ", string);
    }
}
