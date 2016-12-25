package com.ld.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Shuriken extends Projectile {
    
    Player player;
    
    float speed = 6;
    
    Color centerColor = Color.BLACK;
    
    public Shuriken(Map map, Player player, boolean shootingRight){
        super(map);
        this.player = player;
        
        if(shootingRight){
            this.hitbox = new HurtboxRectangle(new Rectangle(player.position.x + Player.PLAYER_WIDTH, player.position.y + 20, 20, 20), this);
            this.head = new Vector2(hitbox.x+10, hitbox.y+10);
            this.horizVelocity = speed;
        }
        else{
            this.hitbox = new HurtboxRectangle(new Rectangle(player.position.x - 21, player.position.y + 20, 20, 20), this);
            this.head = new Vector2(hitbox.x+10, hitbox.y+10);
            this.horizVelocity = -speed;
        }
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
        renderRectHelper(r, this.hitbox);
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
