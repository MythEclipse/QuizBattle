package com.mytheclipse.quizbattle

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.mytheclipse.quizbattle.data.local.entity.Friend
import com.mytheclipse.quizbattle.data.repository.FriendEvent
import com.mytheclipse.quizbattle.data.repository.MatchInviteEvent
import com.mytheclipse.quizbattle.databinding.ActivityFriendListBinding
import com.mytheclipse.quizbattle.ui.FriendAdapter
import com.mytheclipse.quizbattle.ui.FriendRequestAdapter
import com.mytheclipse.quizbattle.viewmodel.FriendAction
import com.mytheclipse.quizbattle.viewmodel.FriendListViewModel
import com.mytheclipse.quizbattle.viewmodel.FriendTab
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FriendListActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityFriendListBinding
    private val viewModel: FriendListViewModel by viewModels()
    
    private lateinit var friendAdapter: FriendAdapter
    private lateinit var requestAdapter: FriendRequestAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFriendListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupTabs()
        setupRecyclerViews()
        setupClickListeners()
        observeState()
        observeActions()
        
        // ViewModel will automatically initialize the user from TokenRepository
    }
    
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
    }
    
    private fun setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> viewModel.setSelectedTab(FriendTab.ALL)
                    1 -> viewModel.setSelectedTab(FriendTab.ONLINE)
                    2 -> viewModel.setSelectedTab(FriendTab.PENDING)
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }
    
    private fun setupRecyclerViews() {
        friendAdapter = FriendAdapter(
            onChatClick = { friend -> viewModel.openChat(friend) },
            onInviteClick = { friend -> showInviteDialog(friend) },
            onRemoveClick = { friend -> showRemoveFriendDialog(friend) }
        )
        
        requestAdapter = FriendRequestAdapter(
            onAcceptClick = { friend -> viewModel.acceptFriendRequest(friend.id) },
            onRejectClick = { friend -> viewModel.rejectFriendRequest(friend.id) }
        )
        
        binding.rvFriends.apply {
            layoutManager = LinearLayoutManager(this@FriendListActivity)
            adapter = friendAdapter
        }
        
        binding.rvPendingRequests.apply {
            layoutManager = LinearLayoutManager(this@FriendListActivity)
            adapter = requestAdapter
        }
    }
    
    private fun setupClickListeners() {
        binding.fabAddFriend.setOnClickListener {
            showAddFriendDialog()
        }
        
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refreshFriendList()
        }
    }
    
    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collectLatest { state ->
                    binding.swipeRefresh.isRefreshing = state.isLoading
                    
                    // Update tab badges
                    binding.tabLayout.getTabAt(1)?.text = "Online (${state.onlineCount})"
                    binding.tabLayout.getTabAt(2)?.apply {
                        text = "Permintaan"
                        if (state.pendingCount > 0) {
                            orCreateBadge.number = state.pendingCount
                        } else {
                            removeBadge()
                        }
                    }
                    
                    // Show appropriate list based on selected tab
                    when (state.selectedTab) {
                        FriendTab.ALL -> {
                            binding.rvFriends.visibility = View.VISIBLE
                            binding.rvPendingRequests.visibility = View.GONE
                            friendAdapter.submitList(state.friends)
                            binding.tvEmpty.visibility = if (state.friends.isEmpty()) View.VISIBLE else View.GONE
                            binding.tvEmpty.text = "Belum ada teman. Tambahkan teman untuk mulai bermain!"
                        }
                        FriendTab.ONLINE -> {
                            binding.rvFriends.visibility = View.VISIBLE
                            binding.rvPendingRequests.visibility = View.GONE
                            friendAdapter.submitList(state.onlineFriends)
                            binding.tvEmpty.visibility = if (state.onlineFriends.isEmpty()) View.VISIBLE else View.GONE
                            binding.tvEmpty.text = "Tidak ada teman online saat ini"
                        }
                        FriendTab.PENDING -> {
                            binding.rvFriends.visibility = View.GONE
                            binding.rvPendingRequests.visibility = View.VISIBLE
                            requestAdapter.submitList(state.pendingReceived)
                            binding.tvEmpty.visibility = if (state.pendingReceived.isEmpty()) View.VISIBLE else View.GONE
                            binding.tvEmpty.text = "Tidak ada permintaan pertemanan"
                        }
                    }
                    
                    state.error?.let { error ->
                        Toast.makeText(this@FriendListActivity, error, Toast.LENGTH_SHORT).show()
                        viewModel.clearError()
                    }
                }
            }
        }
    }
    
    private fun observeActions() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.action.collect { action ->
                    when (action) {
                        is FriendAction.ShowToast -> {
                            Toast.makeText(this@FriendListActivity, action.message, Toast.LENGTH_SHORT).show()
                        }
                        is FriendAction.ShowFriendRequestDialog -> {
                            showFriendRequestReceivedDialog(action.event)
                        }
                        is FriendAction.ShowMatchInviteDialog -> {
                            showMatchInviteDialog(action.event)
                        }
                        is FriendAction.NavigateToMatch -> {
                            // Navigate to online battle activity
                            val intent = Intent(this@FriendListActivity, OnlineBattleActivity::class.java).apply {
                                putExtra("MATCH_ID", action.matchId)
                            }
                            startActivity(intent)
                        }
                        is FriendAction.NavigateToChat -> {
                            val intent = Intent(this@FriendListActivity, ChatRoomActivity::class.java).apply {
                                putExtra("FRIEND_ID", action.friendId)
                                putExtra("FRIEND_NAME", action.friendName)
                                putExtra("IS_PRIVATE", true)
                            }
                            startActivity(intent)
                        }
                    }
                }
            }
        }
    }
    
    private fun showAddFriendDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_friend, null)
        val etUserId = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etUserId)
        val etMessage = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etMessage)
        
        MaterialAlertDialogBuilder(this)
            .setTitle("Tambah Teman")
            .setView(dialogView)
            .setPositiveButton("Kirim") { _, _ ->
                val userId = etUserId.text?.toString()?.trim()
                val message = etMessage.text?.toString()?.trim()
                
                if (!userId.isNullOrEmpty()) {
                    viewModel.sendFriendRequest(userId, message)
                    Toast.makeText(this, "Permintaan pertemanan dikirim", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Masukkan ID pengguna", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }
    
    private fun showInviteDialog(friend: Friend) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Undang ke Battle")
            .setMessage("Undang ${friend.friendName} untuk bertanding?")
            .setPositiveButton("Undang") { _, _ ->
                viewModel.sendMatchInvite(friend.friendId)
                Toast.makeText(this, "Undangan dikirim ke ${friend.friendName}", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Batal", null)
            .show()
    }
    
    private fun showRemoveFriendDialog(friend: Friend) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Hapus Teman")
            .setMessage("Hapus ${friend.friendName} dari daftar teman?")
            .setPositiveButton("Hapus") { _, _ ->
                viewModel.removeFriend(friend.friendId)
            }
            .setNegativeButton("Batal", null)
            .show()
    }
    
    private fun showFriendRequestReceivedDialog(event: FriendEvent.RequestReceived) {
        val message = buildString {
            append("${event.senderName} ingin berteman denganmu")
            if (event.senderPoints > 0) {
                append("\nPoin: ${event.senderPoints}")
            }
            if (!event.message.isNullOrEmpty()) {
                append("\n\nPesan: ${event.message}")
            }
        }
        
        MaterialAlertDialogBuilder(this)
            .setTitle("Permintaan Pertemanan")
            .setMessage(message)
            .setPositiveButton("Terima") { _, _ ->
                viewModel.acceptFriendRequest(event.requestId)
            }
            .setNegativeButton("Tolak") { _, _ ->
                viewModel.rejectFriendRequest(event.requestId)
            }
            .setNeutralButton("Nanti", null)
            .show()
    }
    
    private fun showMatchInviteDialog(event: MatchInviteEvent.InviteReceived) {
        val message = buildString {
            append("${event.senderName} mengundangmu untuk bertanding!")
            append("\n\nPoin: ${event.senderPoints}")
            append("\nKemenangan: ${event.senderWins}")
            append("\n\nPengaturan:")
            append("\n• Kesulitan: ${event.difficulty}")
            append("\n• Kategori: ${event.category}")
            append("\n• Jumlah soal: ${event.totalQuestions}")
            append("\n• Waktu per soal: ${event.timePerQuestion} detik")
            if (!event.message.isNullOrEmpty()) {
                append("\n\nPesan: ${event.message}")
            }
        }
        
        MaterialAlertDialogBuilder(this)
            .setTitle("Undangan Battle")
            .setMessage(message)
            .setPositiveButton("Terima") { _, _ ->
                viewModel.acceptMatchInvite(event.inviteId)
            }
            .setNegativeButton("Tolak") { _, _ ->
                viewModel.rejectMatchInvite(event.inviteId)
            }
            .setCancelable(false)
            .show()
    }
}
