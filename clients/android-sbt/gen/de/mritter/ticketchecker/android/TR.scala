package de.mritter.ticketchecker.android

import scala.language.implicitConversions
import android.app.{Activity,Dialog}
import android.view.{View,ViewGroup,LayoutInflater}

case class TypedResource[A](id: Int)
case class TypedLayout[A](id: Int)

object TR {
  val `connect` = TypedResource[android.widget.Button](R.id.`connect`)
  val `LinearLayout1` = TypedResource[android.widget.LinearLayout](R.id.`LinearLayout1`)
  val `clear` = TypedResource[android.widget.Button](R.id.`clear`)
  val `host` = TypedResource[android.widget.EditText](R.id.`host`)
  val `preview` = TypedResource[android.widget.FrameLayout](R.id.`preview`)
  val `ticket_list` = TypedResource[android.widget.ListView](R.id.`ticket_list`)
  val `text` = TypedResource[android.widget.TextView](R.id.`text`)
  val `checkin_progress` = TypedResource[android.widget.ProgressBar](R.id.`checkin_progress`)

  object layout {
    val `main` = TypedLayout[android.widget.LinearLayout](R.layout.`main`)
    val `ticket_list_item` = TypedLayout[android.widget.LinearLayout](R.layout.`ticket_list_item`)
  }
}

trait TypedViewHolder {
  def findViewById(id: Int): View
  def findView[A](tr: TypedResource[A]): A =
    findViewById(tr.id).asInstanceOf[A]
}

class TypedLayoutInflater(l: LayoutInflater) {
  def inflate[A](tl: TypedLayout[A], c: ViewGroup, b: Boolean) =
    l.inflate(tl.id, c, b).asInstanceOf[A]
  def inflate[A](tl: TypedLayout[A], c: ViewGroup) =
    l.inflate(tl.id, c).asInstanceOf[A]
}

object TypedResource {
  implicit def viewToTyped(v: View) = new TypedViewHolder {
    def findViewById(id: Int) = v.findViewById(id)
  }
  implicit def activityToTyped(a: Activity) = new TypedViewHolder {
    def findViewById(id: Int) = a.findViewById(id)
  }
  implicit def dialogToTyped(d: Dialog) = new TypedViewHolder {
    def findViewById(id: Int) = d.findViewById(id)
  }
  implicit def layoutInflaterToTyped(l: LayoutInflater) =
    new TypedLayoutInflater(l)
}
