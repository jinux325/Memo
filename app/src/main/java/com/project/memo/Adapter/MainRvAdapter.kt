package com.project.memo.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.project.memo.Activity.MemoDetailActivity
import com.project.memo.R
import com.project.memo.VO.MemoMainVO
import kotlinx.android.synthetic.main.activity_main_recyclerview_item.view.*


class MainRvAdapter(itemView: View): RecyclerView.ViewHolder(itemView) {
    val memoTitle = itemView.memoTitle
    val memoContent = itemView.memoContent
    val memoImage = itemView.memoImage
    val cardView = itemView.card_view
}

class Adapter(val context: Context, private val mainRvList:MutableList<MemoMainVO>): RecyclerView.Adapter<MainRvAdapter>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainRvAdapter {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.activity_main_recyclerview_item,parent, false)
        return MainRvAdapter(view)
    }

    override fun getItemCount(): Int {
        return mainRvList.size
    }

    override fun onBindViewHolder(holder: MainRvAdapter, position: Int) {
        val id = mainRvList.get(position).id
        val content = mainRvList.get(position).content
        val title = mainRvList.get(position).title
        val image = mainRvList.get(position).image

        // 상세페이지로 이동 버튼 이벤트
        holder.cardView.setOnClickListener {
            //Toast.makeText(context," $title 클릭 ",Toast.LENGTH_LONG).show()
            val detailIntent = Intent(context, MemoDetailActivity::class.java)
            detailIntent.putExtra("id", id)
            detailIntent.putExtra("title", title)
            detailIntent.putExtra("content", content)
            //detailIntent.putExtra("image",image)
            context.startActivity(detailIntent)
        }

        holder.memoTitle.text = title
        holder.memoContent.text = content
        Glide.with(context).load(image).into(holder.memoImage)
    }

}