package minesweeper;
import CustomSequences.MinesCoor2DArray;
import CustomSequences.SurroundingMines2DArray;
import java.util.LinkedList;
import java.util.Queue;

public class Grid {
    // <__ DATA MEMBERS __> \\
    private int width;
    private int height;
    private int minesCount;
    private Square[][] field;
    private Mine[] mines;
    private Game CurrentGame;

    // <__ CONSTRUCTERS __> \\
    public Grid(int width,int height,int minesNumber) {
        this.width=width+1;
        this.height=height+1;
        this.minesCount = minesNumber;
        InitGrid();
    }

    // <__ METHODS __> \\
    public void InitGrid() {
        field = new Square[height][width];
        //to generate random coordinates for mines
        MinesCoor2DArray minesCoordinates = new MinesCoor2DArray(width, height, Boolean.FALSE);
        minesCoordinates.GenerateRandomMines(minesCount);
        SurroundingMines2DArray numberOfSurroundedmines = new SurroundingMines2DArray(width, height, minesCoordinates);
     
        //init sqaures inside the field
        for (int i = 1 ;i < height; i++) {
            for (int j = 1;j < width; j++) {
                field[i][j] = new Square(i, j,minesCoordinates.arr[i][j],numberOfSurroundedmines.arr[i][j]);
            }
        }
    }
        
    public void AcceptMove(PlayerMove move) {
        move.setSquare(field[move.getSquare().getX()][move.getSquare().getY()]);
        if(move.getType()==MoveType.Mark){
            move.getSquare().ChangeStatus(move.getPlayer(), MoveType.Mark);
        }
        else{
            if(move.getSquare().isMine()){
                move.getSquare().ChangeStatus(move.getPlayer(),MoveType.Reveal);
            }
            else if(move.getSquare().getNumberOfSurroundedMines()!=0){
                move.getSquare().ChangeStatus(move.getPlayer(), MoveType.Reveal);
            }
            else{
                this.floodFill(move);
            }
        }
    }
    private void floodFill(PlayerMove move) {
        Queue<PlayerMove> Q=new LinkedList<PlayerMove>();
        Q.add(move);
        while(!Q.isEmpty()){
            PlayerMove curMove=Q.poll();
            Square curScuare=curMove.getSquare();
            curScuare.ChangeStatus(curMove.getPlayer(), MoveType.Reveal);
            if(curMove.getSquare().getNumberOfSurroundedMines()!=0)continue;
            for(int i=curScuare.getX()-1;i<=curScuare.getX()+1;i++){
                for(int j=curScuare.getY()-1;j<=curScuare.getY()+1;j++){
                    if(!SurroundingMines2DArray.CheckIndex(i,j))continue;;
                    Square toScuare=field[i][j];
                    if(toScuare.getStatus()==SquareStatus.Closed && !toScuare.isMine()){
                        ((LinkedList<PlayerMove>) Q).add(new PlayerMove(move.getPlayer(),toScuare,MoveType.Reveal,new MoveResult()));
                    }
                }
            }
        }
    }
    // <__ SETTERS-GETTERS __> \\
    //Getters
    public int getWidth(){return this.width;}
    public int getHeight(){return this.height;}
    public int getMinesCount() {
        return minesCount;
    }
    public Square[][] getField() { return this.field;}



}
