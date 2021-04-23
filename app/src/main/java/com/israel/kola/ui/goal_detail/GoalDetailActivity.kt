package com.israel.kola.ui.goal_detail

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.israel.kola.R
import com.israel.kola.databinding.ActivityGoalDetailBinding
import com.israel.kola.models.Goal
import com.israel.kola.ui.goal_detail.add_contribution.AddContributionDialog
import kotlinx.android.synthetic.main.activity_goal_detail.*

class GoalDetailActivity : AppCompatActivity() {
    lateinit var viewModel: GoalDetailViewModel
    lateinit var binding: ActivityGoalDetailBinding
    private var contributionAdapter: ContributionAdapter = ContributionAdapter(arrayListOf())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_goal_detail)
        viewModel = ViewModelProviders.of(this).get(GoalDetailViewModel::class.java)
        viewModel.fetchContributions()

        val goal = intent.getParcelableExtra<Goal>("goal")
        binding.goal = goal

        toolbar.setNavigationOnClickListener {
            finish()
        }

        binding.contributionList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = contributionAdapter
        }


        binding.remainingList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = contributionAdapter
        }

        binding.buttonContribute.setOnClickListener{
            val dialog = AddContributionDialog(this)
            dialog.show()
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.contributions.observe(this, Observer {contributions ->
            contributions?.let {
                contributionAdapter.updateContributions(it)
            }
        })
    }
}