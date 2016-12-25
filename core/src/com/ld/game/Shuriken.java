package com.ld.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Shuriken extends Projectile {
    
    Player player;
    
    float speed = 6;
    
    float halfSize = 10;
    float inRadius = 2;
    float medRadius = 4;
    float outRadius = 14;
    
    Color centerColor = Color.BLACK;
    
    public Shuriken(Map map, Player player, boolean shootingRight){
        super(map);
        this.player = player;
        
        if(shootingRight){
            this.head = new Vector2(player.position.x + Player.PLAYER_WIDTH+10, player.position.y + 30);
//            this.hitbox = new HurtboxRectangle(new Rectangle(player.position.x + Player.PLAYER_WIDTH, player.position.y + 20, 20, 20), this);
//            this.head = new Vector2(hitbox.x+10, hitbox.y+10);
            this.horizVelocity = speed;
        }
        else{
            this.head = new Vector2(player.position.x - 11, player.position.y + 30);
//            this.hitbox = new HurtboxRectangle(new Rectangle(player.position.x - 21, player.position.y + 20, 20, 20), this);
//            this.head = new Vector2(hitbox.x+10, hitbox.y+10);
            this.horizVelocity = -speed;
        }
        
        this.hitbox = new HurtboxRectangle(new Rectangle(this.head.x-halfSize, this.head.y-halfSize, 2*halfSize, 2*halfSize), this);
    }
    
    @Override
    public void update() {
        super.update();
        if(this.intersectPlayer()){
            this.destroy();
            player.grabShuriken();
        }
    }
    
    private boolean intersectPlayer() {
        return Intersector.overlaps(player.position, this.hitbox);
    }
    
    @Override
    public void render(ShapeRenderer r) {
        r.setColor(centerColor);
        float x = this.head.x;
        float y = this.head.y;
//        float[] vertices = {4, 4, 0, 14, -4, 4, -14, 0, -4, -4, 0, -14, 4, -4, 14, 0};
        float[] vs1 = {medRadius, medRadius, 0, outRadius, -medRadius, medRadius};
        r.triangle(vs1[0]+x, vs1[1]+y, vs1[2]+x, vs1[3]+y, vs1[4]+x, vs1[5]+y);
        float[] vs2 = {-medRadius, medRadius, -outRadius, 0, -medRadius, -medRadius};
        r.triangle(vs2[0]+x, vs2[1]+y, vs2[2]+x, vs2[3]+y, vs2[4]+x, vs2[5]+y);
        float[] vs3 = {-medRadius, -medRadius, 0, -outRadius, medRadius, -medRadius};
        r.triangle(vs3[0]+x, vs3[1]+y, vs3[2]+x, vs3[3]+y, vs3[4]+x, vs3[5]+y);
        float[] vs4 = {medRadius, -medRadius, outRadius, 0, medRadius, medRadius};
        r.triangle(vs4[0]+x, vs4[1]+y, vs4[2]+x, vs4[3]+y, vs4[4]+x, vs4[5]+y);
        
        r.rect(inRadius+x, -medRadius+y, medRadius-inRadius, 2*medRadius-inRadius);
        r.rect(-medRadius+x, -medRadius+inRadius+y, medRadius-inRadius, 2*medRadius-inRadius);
        r.rect(-medRadius+x, -medRadius+y, 2*medRadius-inRadius, medRadius-inRadius);
        r.rect(-medRadius+inRadius+x, inRadius+y, 2*medRadius-inRadius, medRadius-inRadius);
        
//        float[] verticesTranslated = new float[16];
//        int i=0;
//        while(i<16){
//            verticesTranslated[i] = vertices[i] + this.head.x;
//            i++;
//            verticesTranslated[i] = vertices[i] + this.head.y;
//            i++;
//        }
        
        r.triangle(4 + this.head.x, 4 + this.head.y, 0 + this.head.x, 14 + this.head.y, -4 + this.head.x, 4 + this.head.y);
        
//        renderRectHelper(r, this.hitbox);
    }
    
    public void stop() {
        this.horizVelocity = 0;
        this.vertVelocity = 0;
    }
    
    @Override
    public void destroy() {
        player.removeHurtbox(this.hitbox);
        super.destroy();
    }
    
    @Override
    public void handleWallCollision() {
        this.stop();
    }

}
